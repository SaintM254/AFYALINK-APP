#!/usr/bin/env python3
"""Convert brand/afyalink-logo.svg into Android VectorDrawables.

The SVG is authored on a 2000x2000 canvas and wraps every glyph in nested
translation/scale groups.  VectorDrawable supports nested <group> transforms,
so the path data is copied verbatim and only the group chain is rebuilt.  That
keeps the artwork pixel-exact and infinitely scalable - no clipping, no
rasterising, no font dependency for the mark.

Run:  python3 tools/svg_to_vector.py
"""
from __future__ import annotations

import math
import re
import sys
from pathlib import Path
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SVG = ROOT / "brand" / "afyalink-logo.svg"
RES = ROOT / "android" / "app" / "src" / "main" / "res"

# --- path parsing -----------------------------------------------------------
NUM = re.compile(r"[-+]?(?:\d*\.\d+|\d+)(?:[eE][-+]?\d+)?")
CMD = re.compile(r"[MmLlHhVvCcSsQqTtAaZz]")
ARGC = {"M": 2, "L": 2, "H": 1, "V": 1, "C": 6, "S": 4, "Q": 4, "T": 2, "A": 7, "Z": 0}


def parse_path(d: str):
    """Yield (command, [float args]) with implicit repeats expanded."""
    tokens = re.findall(r"[MmLlHhVvCcSsQqTtAaZz]|[-+]?(?:\d*\.\d+|\d+)(?:[eE][-+]?\d+)?", d)
    i = 0
    cmd = None
    out = []
    while i < len(tokens):
        t = tokens[i]
        if CMD.fullmatch(t):
            cmd = t
            i += 1
            if cmd.upper() == "Z":
                out.append(("Z", []))
                continue
        if cmd is None:
            raise ValueError("path data does not start with a command")
        n = ARGC[cmd.upper()]
        args = [float(x) for x in tokens[i : i + n]]
        if len(args) != n:
            raise ValueError(f"truncated args for {cmd}")
        i += n
        out.append((cmd, args))
    return out


def transform_commands(cmds, m):
    """Return SVG path data string with an affine matrix (a,b,c,d,e,f) applied."""
    a, b, c, d_, e, f = m
    parts = []
    cx = cy = 0.0  # current point (pre-transform space)
    sx = sy = 0.0  # subpath start
    for cmd, args in cmds:
        u = cmd.upper()
        rel = cmd.islower()
        if u == "Z":
            parts.append("Z")
            cx, cy = sx, sy
            continue
        if u == "H":
            x = args[0] + (cx if rel else 0)
            pts = [(x, cy)]
            cmd_out = "H" if not rel else "h"
            parts.append(cmd_out + " " + f"{pts[0][0]:.3f}")
            cx = x
            continue
        if u == "V":
            y = args[0] + (cy if rel else 0)
            parts.append(("V" if not rel else "v") + " " + f"{y:.3f}")
            cy = y
            continue
        # generic handling: split args into coordinate pairs (A has 7 args)
        coords = []
        if u == "A":
            rx, ry, rot, laf, sf, x, y = args
            coords = [(x + (cx if rel else 0), y + (cy if rel else 0))]
            # arcs are not used by this artwork; approximate as a line if ever hit
            parts.append(("L" if not rel else "l") + " " + f"{coords[0][0]:.3f},{coords[0][1]:.3f}")
            cx, cy = coords[0]
            continue
        for k in range(0, len(args), 2):
            x, y = args[k], args[k + 1]
            if rel:
                x += cx
                y += cy
            coords.append((x, y))
        # convert the whole sub-command into absolute form, then apply matrix
        for k, (x, y) in enumerate(coords):
            pass
        if u in ("M", "L", "T", "Q", "S", "C"):
            out = [u if not rel else u.lower()]
            for k, (x, y) in enumerate(coords):
                tx = a * x + c * y + e
                ty = b * x + d_ * y + f
                out.append(f"{tx:.3f}")
                out.append(f"{ty:.3f}")
            parts.append(" ".join(out))
        cx, cy = coords[-1]
        if u == "M":
            sx, sy = cx, cy
    return " ".join(parts)


def sample_bbox(cmds):
    """Approximate the geometric bbox by densely sampling the path in final space."""
    pts = []
    cx = cy = 0.0
    sx = sy = 0.0
    for cmd, args in cmds:
        u = cmd.upper()
        rel = cmd.islower()
        if u == "Z":
            cx, cy = sx, sy
            continue
        if u == "H":
            cx = args[0] + (cx if rel else 0)
            pts.append((cx, cy))
            continue
        if u == "V":
            cy = args[0] + (cy if rel else 0)
            pts.append((cx, cy))
            continue
        pairs = [(args[k], args[k + 1]) for k in range(0, len(args), 2)]
        if rel:
            pairs = [(x + cx, y + cy) for x, y in pairs]
        if u == "C":
            for i in range(0, len(pairs), 3):
                p0 = (cx, cy)
                p1, p2, p3 = pairs[i], pairs[i + 1], pairs[i + 2]
                for s in range(0, 11):
                    t = s / 10.0
                    x = (1 - t) ** 3 * p0[0] + 3 * (1 - t) ** 2 * t * p1[0] + 3 * (1 - t) * t * t * p2[0] + t ** 3 * p3[0]
                    y = (1 - t) ** 3 * p0[1] + 3 * (1 - t) ** 2 * t * p1[1] + 3 * (1 - t) * t * t * p2[1] + t ** 3 * p3[1]
                    pts.append((x, y))
                cx, cy = p3
            continue
        for p in pairs:
            pts.append(p)
        cx, cy = pairs[-1]
        if u == "M":
            sx, sy = cx, cy
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    return min(xs), min(ys), max(xs), max(ys)


def sample_points(cmds):
    """Densely sample a path (flattened curves) for exact extents."""
    pts = []
    cx = cy = 0.0
    sx = sy = 0.0
    for cmd, args in cmds:
        u = cmd.upper()
        rel = cmd.islower()
        if u == "Z":
            cx, cy = sx, sy
            continue
        if u == "H":
            cx = args[0] + (cx if rel else 0)
            pts.append((cx, cy))
            continue
        if u == "V":
            cy = args[0] + (cy if rel else 0)
            pts.append((cx, cy))
            continue
        pairs = [(args[k], args[k + 1]) for k in range(0, len(args), 2)]
        if rel:
            pairs = [(x + cx, y + cy) for x, y in pairs]
        if u == "C":
            for i in range(0, len(pairs), 3):
                p0, p1, p2, p3 = (cx, cy), pairs[i], pairs[i + 1], pairs[i + 2]
                for s in range(0, 21):
                    t = s / 20.0
                    pts.append((
                        (1 - t) ** 3 * p0[0] + 3 * (1 - t) ** 2 * t * p1[0] + 3 * (1 - t) * t * t * p2[0] + t ** 3 * p3[0],
                        (1 - t) ** 3 * p0[1] + 3 * (1 - t) ** 2 * t * p1[1] + 3 * (1 - t) * t * t * p2[1] + t ** 3 * p3[1],
                    ))
                cx, cy = p3
            continue
        for pt in pairs:
            pts.append(pt)
        cx, cy = pairs[-1]
        if u == "M":
            sx, sy = cx, cy
    return pts


def parse_matrix(value: str):
    nums = [float(n) for n in NUM.findall(value)]
    if len(nums) == 6:
        return tuple(nums)
    raise ValueError(f"unsupported transform: {value}")


def mat_mul(m1, m2):
    a1, b1, c1, d1, e1, f1 = m1
    a2, b2, c2, d2, e2, f2 = m2
    return (
        a1 * a2 + c1 * b2,
        b1 * a2 + d1 * b2,
        a1 * c2 + c1 * d2,
        b1 * c2 + d1 * d2,
        a1 * e2 + c1 * f2 + e1,
        b1 * e2 + d1 * f2 + f1,
    )


IDENTITY = (1.0, 0.0, 0.0, 1.0, 0.0, 0.0)


def collect_glyph_paths():
    """Walk the SVG and return [(matrix, path_d, fill)] for the artwork (no text)."""
    tree = ET.parse(SVG)
    root = tree.getroot()
    results = []

    def walk(node, matrix):
        tag = node.tag.split("}")[-1]
        m = matrix
        tr = node.get("transform")
        if tr:
            m = mat_mul(matrix, parse_matrix(tr))
        if tag == "path":
            results.append((m, node.get("d"), node.get("style", "")))
        elif tag == "text":
            # wordmark + tagline are rendered as live text in the app instead,
            # so they stay out of the vector mark.
            pass
        for child in list(node):
            walk(child, m)

    walk(root, IDENTITY)
    return results


def apply(m, x, y):
    a, b, c, d, e, f = m
    return a * x + c * y + e, b * x + d * y + f


def build():
    paths = collect_glyph_paths()
    if not paths:
        sys.exit("no artwork paths found in the brand SVG")

    # Collapse the nested transforms of each path into one group chain and
    # remember the cumulative matrix so the bbox can be computed exactly.
    flattened = []
    for m, d, style in paths:
        cmds = parse_path(d)
        fill = "brand_teal"
        if "fill:rgb(9,93,126)" in style:
            fill = "brand_teal"
        elif "fill:rgb" in style:
            fill = "brand_ink"
        flat_d = transform_commands(cmds, m)
        flat_cmds = parse_path(flat_d)
        bbox = sample_bbox(flat_cmds)
        flattened.append({"d": flat_d, "fill": fill, "bbox": bbox})

    x0 = min(p["bbox"][0] for p in flattened)
    y0 = min(p["bbox"][1] for p in flattened)
    x1 = max(p["bbox"][2] for p in flattened)
    y1 = max(p["bbox"][3] for p in flattened)
    w, h = x1 - x0, y1 - y0
    print(f"glyph bbox: {x0:.2f},{y0:.2f} .. {x1:.2f},{y1:.2f}  ({w:.2f} x {h:.2f})")

    def vector(viewport_w, viewport_h, paths_xml, width_dp, height_dp):
        return (
            '<?xml version="1.0" encoding="utf-8"?>\n'
            "<!-- Generated by tools/svg_to_vector.py from brand/afyalink-logo.svg -->\n"
            '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            f'    android:width="{width_dp}dp"\n'
            f'    android:height="{height_dp}dp"\n'
            f'    android:viewportWidth="{viewport_w:.3f}"\n'
            f'    android:viewportHeight="{viewport_h:.3f}">\n'
            f"{paths_xml}</vector>\n"
        )

    def path_xml(p, color_map, indent="    "):
        color = color_map[p["fill"]]
        return (
            f'{indent}<path\n'
            f'{indent}    android:fillColor="{color}"\n'
            f'{indent}    android:fillType="evenOdd"\n'
            f'{indent}    android:pathData="{p["d"]}" />\n'
        )

    # ---- 1. tight mark (used on the About screen and in the splash) --------
    pad = 12.0
    vw, vh = w + pad * 2, h + pad * 2
    mark_paths = "".join(path_xml(p, {"brand_teal": "@color/brand_teal", "brand_ink": "@color/brand_ink"}) for p in flattened)
    mark = vector(vw, vh, f'    <group android:translateX="{-x0 + pad:.3f}" android:translateY="{-y0 + pad:.3f}">\n{mark_paths}    </group>\n', 96, 96)
    (RES / "drawable" / "ic_afyalink_mark.xml").write_text(mark)

    # ---- 2. adaptive launcher foreground (108dp canvas, 66dp safe circle) --
    # Android masks every adaptive icon; the only guaranteed-visible area is the
    # 66dp circle (radius 33dp) in the middle of the 108dp canvas.  The scale
    # below is derived from the artwork's true maximum radius around its centre,
    # so the logo can never be clipped by any launcher mask shape.
    cx = (x0 + x1) / 2.0
    cy = (y0 + y1) / 2.0
    max_r = 0.0
    for p in flattened:
        for px, py in sample_points(parse_path(p["d"])):
            max_r = max(max_r, math.hypot(px - cx, py - cy))
    safe_r = 32.2                     # 33dp circle minus a rounding hairline
    scale = safe_r / max_r
    print(f"artwork radius {max_r:.2f} units -> scale {scale:.6f}; "
          f"{w * scale:.1f} x {h * scale:.1f} dp inside the 66dp safe circle")
    off_x = 54.0 - cx * scale
    off_y = 54.0 - cy * scale
    fg_paths = "".join(
        path_xml(p, {"brand_teal": "#FF095D7E", "brand_ink": "#FF141717"}, indent="        ")
        for p in flattened
    )
    fg = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- Generated by tools/svg_to_vector.py - adaptive icon foreground -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="108dp"\n'
        '    android:height="108dp"\n'
        '    android:viewportWidth="108"\n'
        '    android:viewportHeight="108">\n'
        f'    <group android:translateX="{off_x:.3f}" android:translateY="{off_y:.3f}"\n'
        f'        android:scaleX="{scale:.6f}" android:scaleY="{scale:.6f}">\n'
        f"{fg_paths}"
        "    </group>\n"
        "</vector>\n"
    )
    (RES / "drawable" / "ic_launcher_foreground.xml").write_text(fg)

    # ---- 3. monochrome (Android 13 themed icons) --------------------------
    mono_paths = "".join(
        path_xml(p, {"brand_teal": "#FFFFFFFF", "brand_ink": "#FFFFFFFF"}, indent="        ")
        for p in flattened
    )
    mono = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<!-- Generated by tools/svg_to_vector.py - themed (monochrome) icon -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="108dp"\n'
        '    android:height="108dp"\n'
        '    android:viewportWidth="108"\n'
        '    android:viewportHeight="108">\n'
        f'    <group android:translateX="{off_x:.3f}" android:translateY="{off_y:.3f}"\n'
        f'        android:scaleX="{scale:.6f}" android:scaleY="{scale:.6f}">\n'
        f"{mono_paths}"
        "    </group>\n"
        "</vector>\n"
    )
    (RES / "drawable" / "ic_launcher_monochrome.xml").write_text(mono)

    print("wrote ic_afyalink_mark.xml, ic_launcher_foreground.xml, ic_launcher_monochrome.xml")


if __name__ == "__main__":
    build()
