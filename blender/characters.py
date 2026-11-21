import bpy
import sys, math, bmesh
import os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from lib import *
from mathutils import Vector

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'mobile', 'assets', 'models') + os.sep
HEAD_R = (1.0, 0.95, 1.45)        # x (width), y (depth), z (height)  -- chibi egg head
S = 2.0 / 954; CX, CY = 1000, 912; CROP = (470, 590, 1060)   # matches faces.py (drawing -> face texture)

def trim(obj, keep):
    """Delete faces whose centre fails keep(centre)."""
    bm = bmesh.new(); bm.from_mesh(obj.data)
    dead = [f for f in bm.faces if not keep(f.calc_center_median())]
    bmesh.ops.delete(bm, geom=dead, context='FACES')
    bm.to_mesh(obj.data); bm.free()

def hairline(y):            # front of the face is -Y: high at the forehead, lower at the nape
    t = max(0.0, min(1.0, (-y + 0.35) / 1.0))
    return -0.45 + t * (0.62 + 0.45)

def head_ellipsoid(name, scale=1.0, m='skin', seg=48, rings=32):
    return sphere(name, 1, (0, 0, 0), tuple(k * scale for k in HEAD_R), m=m, seg=seg, rings=rings)

def face_patch():
    o = head_ellipsoid('Face', 1.012, m='white', seg=64, rings=48)
    trim(o, lambda c: c.y < -0.2 and c.z < 0.70)
    me = o.data
    while me.uv_layers: me.uv_layers.remove(me.uv_layers[0])
    uv = me.uv_layers.new(name='UVMap')
    for loop in me.loops:
        v = me.vertices[loop.vertex_index].co
        px, py = CX + v.x / S, CY - v.z / S
        uv.data[loop.index].uv = ((px - CROP[0]) / CROP[2], 1 - (py - CROP[1]) / CROP[2])
    o.data.materials.clear(); o.data.materials.append(bpy.data.materials.new('face'))
    return o

def kaloy_hair():
    cap = sphere('HairCap', 1, (0, 0, 0), (1.06, 1.02, 1.50), m='hair', seg=48, rings=32)
    trim(cap, lambda c: c.z > hairline(c.y))
    sol = cap.modifiers.new('solid', 'SOLIDIFY'); sol.thickness = 0.06; sol.offset = 1
    parts = [cap]
    # clean zig-zag fringe along the front hairline (the drawing's bangs)
    xs = [-0.66, -0.33, 0.0, 0.33, 0.66]
    for i, x in enumerate(xs):
        # point on the hair cap at this x along the hairline
        zc = 0.62 - 0.08 * abs(x)
        yc = -1.02 * math.sqrt(max(0.0, 1 - (x / 1.06) ** 2 - (zc / 1.5) ** 2)) + 0.02
        c = cone(f'Bang{i}', 0.2, 0.0, 0.28, (x, yc, zc - 0.05), (180 - 12 * math.copysign(1, x) * (abs(x) > 0.5), 0, 0), m='hair', verts=16)
        c.scale = (1, 0.55, 1)
        c.rotation_euler.y = math.radians(-14 * x)
        parts.append(c)
    return parts

def ears(m='skin'):
    return [sphere(f'Ear{s}', 1, (0.98 * s, 0.02, -0.15), (0.11, 0.14, 0.22), m=m, seg=16, rings=10) for s in (-1, 1)]

def body(shirt='shirt', pants='pants', shoes='tsinelas', skin='skin', apron=True, sleeves=True, belly=1.0):
    parts = []
    prof = [(0, 0.85), (0.80 * belly, 0.85), (0.93 * belly, 1.08), (0.97 * belly, 1.45), (0.9, 1.82), (0.62, 2.04), (0.3, 2.12), (0, 2.12)]
    torso = lathe('Torso', prof, m=shirt, seg=48)
    parts.append(torso)
    if apron:
        ap = lathe('Apron', [(0, 0.82), (0.83 * belly, 0.82), (0.96 * belly, 1.07), (1.0 * belly, 1.45), (0.93, 1.78), (0, 1.78)], m='apron', seg=48)
        trim(ap, lambda c: c.y < -0.12 and 0.8 < c.z < 1.79 and abs(c.x) < 0.86)
        s = ap.modifiers.new('solid', 'SOLIDIFY'); s.thickness = 0.03; s.offset = 1
        band = lathe('ApronTrim', [(0, 0.80), (0.86 * belly, 0.80), (0.9 * belly, 0.93), (0, 0.93)], m='apron_trim', seg=48)
        trim(band, lambda c: c.y < -0.12 and abs(c.x) < 0.88 and 0.79 < c.z < 0.94)
        s = band.modifiers.new('solid', 'SOLIDIFY'); s.thickness = 0.035; s.offset = 1
        pocket = box('ApronPocket', (0.5, 0.06, 0.3), (0, -0.99 * belly, 1.2), m='apron_trim', bevel=0.03)
        strap = torus('ApronStrap', 0.45, 0.035, (0, 0.05, 1.98), (12, 0, 0), m='apron', mseg=8)
        parts += [ap, band, pocket, strap]
    for s in (-1, 1):
        if sleeves: parts.append(sphere(f'Sleeve{s}', 0.34, (0.92 * s, 0, 1.86), (1, 1, 0.9), m=shirt, seg=24, rings=12))
        parts.append(capsule(f'Leg{s}', 0.3, 0.55, (0.36 * s, 0, 0.38), (0.36 * s, 0, 0.88), m=pants))
        sh = box(f'Shoe{s}', (0.42, 0.75, 0.1), (0.36 * s, -0.2, 0.05), m=shoes, bevel=0.04)
        st = torus(f'Strap{s}', 0.15, 0.04, (0.36 * s, -0.33, 0.1), (0, 90, 0), m=shoes, mseg=6, scale=(1, 1, 0.8))
        parts += [sh, st]
    return parts

def arm(side, skin='skin', sleeve=None):
    """Returns an empty 'Arm{L|R}' pivoting at the shoulder, with arm + hand."""
    s = -1 if side == 'R' else 1   # character's right arm is on -X
    sh = Vector((1.0 * s, 0, 1.78))
    a = capsule(f'UpperArm{side}', 0.17, 0.6, (0, 0, 0), (0.2 * s, -0.15, -0.62), m=skin)
    h = sphere(f'Hand{side}', 0.22, (0.26 * s, -0.2, -0.8), m=skin, seg=24, rings=12)
    piv = group(f'Arm{side}', [a, h], loc=sh)
    return piv

def simple_face(eye='black', mouth='mouth', glasses=False, mustache=False):
    parts = []
    for s in (-1, 1):
        parts.append(sphere(f'Eye{s}', 0.11, (0.36 * s, -0.9, 0.15), (0.8, 0.5, 1.15), m=eye, seg=16, rings=10))
        parts.append(sphere(f'Blush{s}', 0.16, (0.58 * s, -0.74, -0.28), (1, 0.3, 0.6), m='pink', seg=16, rings=8))
    sm = torus('Smile', 0.2, 0.035, (0, -0.86, -0.42), (90, 0, 0), m=mouth, mseg=8)
    trim(sm, lambda c: c.y < 0)
    parts.append(sm)
    if glasses:
        for s in (-1, 1): parts.append(torus(f'Lens{s}', 0.2, 0.03, (0.36 * s, -0.97, 0.15), (90, 0, 0), m='black', mseg=8))
        parts.append(box('Bridge', (0.3, 0.04, 0.04), (0, -0.99, 0.18), m='black', bevel=0.01))
    if mustache:
        for s in (-1, 1): parts.append(sphere(f'Stache{s}', 0.16, (0.13 * s, -0.9, -0.22), (1.2, 0.4, 0.45), (0, 25 * s, 0), m='hair', seg=16, rings=8))
    return parts

def finish_character(name, head_parts, body_parts, arms, head_z, scale):
    head = group('Head', head_parts, loc=(0, 0, head_z))
    bodyg = group('Body', body_parts)
    root = group(name, [head, bodyg] + arms)
    root.scale = (scale, scale, scale)
    return root

def build_kaloy():
    reset()
    hp = [head_ellipsoid('HeadSkin'), face_patch()] + kaloy_hair() + ears()
    # short neck so the head never floats
    neck = cyl('Neck', 0.32, 0.4, (0, 0, -1.35), m='skin', bevel=0)
    hp.append(neck)
    bp = body()
    arms = [arm('L'), arm('R')]
    finish_character('MangKaloy', hp, bp, arms, head_z=3.45, scale=0.25)
    print('kaloy tris', stats())
    export(OUT + 'mang_kaloy.glb')

CUSTOMERS = {
    # name: (skin, shirt, pants, shoes, hair style, extras)
    'customer_suki':    dict(skin='skin', shirt='blue', pants='pants', shoes='tsinelas', hair='short', apron=False),
    'customer_student': dict(skin='wall2', shirt='white', pants='black', shoes='black', hair='short', backpack=True),
    'customer_tita':    dict(skin='wall2', shirt='pink', pants='plastic_red', shoes='red', hair='bun', glasses=True),
    'customer_driver':  dict(skin='brown', shirt='yellow', pants='pants', shoes='tsinelas', hair='cap', towel=True, mustache=True, belly=1.12),
}

def customer_hair(style):
    parts = []
    if style in ('short', 'bun'):
        cap = sphere('Hair', 1, (0, 0, 0), (1.05, 1.0, 1.49), m='hair', seg=40, rings=24)
        cut = 0.55 if style == 'short' else 0.35
        trim(cap, lambda c: c.z > (cut if c.y < -0.3 else -0.6 if style == 'bun' else -0.2))
        s = cap.modifiers.new('solid', 'SOLIDIFY'); s.thickness = 0.06; s.offset = 1
        parts.append(cap)
        if style == 'bun':
            parts.append(sphere('Bun', 0.45, (0, 0.55, 1.15), m='hair', seg=24, rings=14))
    elif style == 'cap':
        parts.append(sphere('CapDome', 1, (0, 0, 0.3), (1.08, 1.04, 1.25), m='red', seg=40, rings=24))
        trim(parts[-1], lambda c: c.z > 0.65)
        s = parts[-1].modifiers.new('solid', 'SOLIDIFY'); s.thickness = 0.06; s.offset = 1
        parts.append(cyl('CapBrim', 0.75, 0.06, (0, -0.95, 0.85), (8, 0, 0), m='red', bevel=0.02, scale=(1, 0.7, 1)))
    return parts

def build_customer(name, cfg):
    reset()
    skin = cfg['skin']
    hp = [head_ellipsoid('HeadSkin', m=skin)] + customer_hair(cfg['hair']) + ears(skin)
    hp += simple_face(glasses=cfg.get('glasses', False), mustache=cfg.get('mustache', False))
    hp.append(cyl('Neck', 0.32, 0.4, (0, 0, -1.35), m=skin, bevel=0))
    bp = body(shirt=cfg['shirt'], pants=cfg['pants'], shoes=cfg['shoes'], apron=False, belly=cfg.get('belly', 1.0))
    if cfg.get('backpack'):
        bp.append(box('Backpack', (1.1, 0.5, 1.0), (0, 0.85, 1.45), m='plastic_blue', bevel=0.12))
        bp.append(box('BackpackPocket', (0.7, 0.18, 0.4), (0, 1.12, 1.25), m='yellow', bevel=0.06))
    if cfg.get('towel'):
        bp.append(torus('NeckTowel', 0.62, 0.11, (0, 0, 2.0), (0, 0, 0), m='white', mseg=10, scale=(1, 1, 0.6)))
    arms = [arm('L', skin), arm('R', skin)]
    finish_character(name, hp, bp, arms, head_z=3.45, scale=0.23)
    print(name, 'tris', stats())
    export(OUT + name + '.glb')

if __name__ == '__main__':
    build_kaloy()
    for n, c in CUSTOMERS.items(): build_customer(n, c)
