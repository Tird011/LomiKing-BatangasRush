"""LomiKing world kit: the barangay around the lomihan (trees, palms, houses, poles, Taal volcano...).

Every piece is one joined object at the origin, base on the ground. The app instances them around each room
(mobile/src/three/world/Surroundings.ts), bakes material colours into vertex colours and draws each piece in a
single draw call. Parts whose material is 'bulb' are split off by the app and glow at night.

    blender --background --python blender/world_kit.py
"""
import bpy, bmesh, sys, os, math, random
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from lib import *
from mathutils import Vector, Matrix
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'mobile', 'assets', 'models') + os.sep
R = random.Random(11)

# Background pieces are seen small and often: no bevels (each bevel multiplies faces), low segment counts.
import lib as _lib
def box(name, size, loc=(0, 0, 0), rot=(0, 0, 0), m='white', bevel=0.0, smooth=True):
    return _lib.box(name, size, loc, rot, m, 0, smooth)
def cyl(name, r, h, loc=(0, 0, 0), rot=(0, 0, 0), m='white', verts=8, bevel=0.0, scale=(1, 1, 1)):
    return _lib.cyl(name, r, h, loc, rot, m, min(verts, 10), 0, scale)
def sphere(name, r=1, loc=(0, 0, 0), scale=(1, 1, 1), rot=(0, 0, 0), m='white', seg=8, rings=5):
    return _lib.sphere(name, r, loc, scale, rot, m, min(seg, 8), min(rings, 5))

def blob(name, r, loc, scale=(1, 1, 1), m='leaf_mid', subdiv=1, jitter=0.18, flat=True):
    """Faceted, slightly lumpy blob: the chunky low-poly foliage look from the references."""
    bm = bmesh.new()
    bmesh.ops.create_icosphere(bm, subdivisions=subdiv, radius=r)
    for v in bm.verts:
        v.co *= 1 + R.uniform(-jitter, jitter)
        v.co.x *= scale[0]; v.co.y *= scale[1]; v.co.z *= scale[2]
    me = bpy.data.meshes.new(name); bm.to_mesh(me); bm.free()
    o = bpy.data.objects.new(name, me); bpy.context.collection.objects.link(o)
    o.location = Vector(loc)
    o.data.materials.append(mat(m))
    for p in o.data.polygons: p.use_smooth = not flat
    return o

def limb(name, a, b, r1, r2, m='trunk', verts=8):
    """Tapered cylinder from point a to point b."""
    a, b = Vector(a), Vector(b); d = b - a
    bpy.ops.mesh.primitive_cone_add(vertices=verts, radius1=r1, radius2=r2, depth=d.length)
    o = bpy.context.active_object
    o.rotation_euler = d.to_track_quat('Z', 'Y').to_euler()
    o.location = (a + b) / 2
    bpy.ops.object.transform_apply(location=False, rotation=True, scale=True)
    o.name = name; o.data.materials.clear(); o.data.materials.append(mat(m))
    return o

def bend_path(points, r0, r1, m='trunk', verts=8):
    out = []
    for i in range(len(points) - 1):
        t0, t1 = i / (len(points) - 1), (i + 1) / (len(points) - 1)
        out.append(limb('Seg', points[i], points[i + 1], r0 + (r1 - r0) * t0, r0 + (r1 - r0) * t1, m, verts))
        if i < len(points) - 2: out.append(sphere('Knot', r0 + (r1 - r0) * t1, points[i + 1], m=m, seg=verts, rings=4))
    return out

def make(name, parts):
    o = join(name, parts); o.location = (0, 0, 0)
    bpy.ops.object.select_all(action='DESELECT'); o.select_set(True)
    bpy.context.view_layer.objects.active = o
    bpy.ops.object.transform_apply(location=True, rotation=True, scale=True)
    print(f'  {name}: {len(o.data.polygons)} faces')
    return o

# ------------------------------------------------------------------------------------------ trees
def acacia():
    """The big umbrella tree (like the reference painting): bent trunk, flat layered canopy."""
    p = bend_path([(0, 0, 0), (0.1, 0, 1.0), (0.45, 0.1, 2.0), (0.4, 0.15, 2.9), (0.1, 0.2, 3.5)], 0.32, 0.16)
    p += [limb('Branch', (0.4, 0.15, 2.9), (1.6, 0.4, 3.7), 0.14, 0.07), limb('Branch', (0.45, 0.1, 2.3), (-1.3, -0.3, 3.3), 0.13, 0.06)]
    for (x, y, z, s) in [(0.1, 0.2, 3.9, 1.0), (1.6, 0.4, 3.9, 0.75), (-1.3, -0.3, 3.5, 0.7), (0.6, -0.9, 3.7, 0.6), (-0.4, 1.0, 3.8, 0.65)]:
        p.append(blob('Canopy', 1.25 * s, (x, y, z), (1.45, 1.3, 0.42), 'leaf_dark', 1, 0.12))
        p.append(blob('CanopyTop', 0.95 * s, (x + 0.15, y - 0.1, z + 0.28 * s), (1.35, 1.2, 0.36), 'leaf_light', 1, 0.16))
        p.append(blob('CanopySun', 0.45 * s, (x + 0.35, y - 0.35, z + 0.42 * s), (1.3, 1.1, 0.4), 'leaf_sun', 1, 0.2))
    return make('TreeAcacia', p)

def mango():
    p = bend_path([(0, 0, 0), (0.05, 0, 0.9), (-0.1, 0.05, 1.7)], 0.26, 0.17)
    p += [limb('Branch', (-0.1, 0.05, 1.5), (0.6, 0.2, 2.3), 0.12, 0.07), limb('Branch', (-0.1, 0.05, 1.6), (-0.7, -0.2, 2.4), 0.12, 0.07)]
    for (x, y, z, r) in [(0, 0, 2.7, 1.15), (0.75, 0.2, 2.4, 0.85), (-0.8, -0.2, 2.5, 0.85), (0.1, 0.7, 2.4, 0.8), (0.1, -0.7, 2.35, 0.8), (0, 0, 3.3, 0.75)]:
        p.append(blob('Canopy', r, (x, y, z), (1, 1, 0.85), 'leaf_mid' if z < 3 else 'leaf_light', 1, 0.14))
    for k in range(5):
        a = R.uniform(0, math.tau)
        p.append(blob('Lit', 0.32, (0.9 * math.cos(a), 0.9 * math.sin(a) - 0.2, 2.75 + R.uniform(0, 0.5)), (1, 1, 0.7), 'leaf_sun', 1, 0.2))
    return make('TreeMango', p)

def palm():
    pts = [(0, 0, 0)]
    for k in range(1, 8): pts.append((0.06 * k * k * 0.22, 0, k * 0.72))
    p = bend_path(pts, 0.2, 0.13, 'palm_trunk', 9)
    for k in range(1, 8, 2): p.append(cyl('Ring', 0.2 - k * 0.009, 0.05, pts[k], m='straw_dark', verts=9, bevel=0))
    top = Vector(pts[-1])
    for i in range(9):
        a = i / 9 * math.tau + R.uniform(-0.1, 0.1)
        d = Vector((math.cos(a), math.sin(a), 0))
        prev = top
        for s in range(3):
            q = top + d * (0.55 * (s + 1)) + Vector((0, 0, 0.25 - 0.18 * s * s))
            p.append(limb('Rib', prev, q, 0.03, 0.02, 'leaf_dark', 5))
            mid = (prev + q) / 2
            lf = blob('Frond', 0.34 - s * 0.05, mid, (1.5, 0.55, 0.12), 'banana_leaf' if s < 2 else 'leaf_mid', 0, 0.1)
            lf.rotation_euler = (0, -math.atan2(q.z - prev.z, 0.55) , a)
            p.append(lf); prev = q
    for k in range(3): p.append(sphere('Coconut', 0.12, (top.x + 0.13 * math.cos(k * 2.1), 0.13 * math.sin(k * 2.1), top.z - 0.15), m='coconut', seg=10, rings=7))
    return make('Palm', p)

def banana():
    p = [limb('Stem', (0, 0, 0), (0, 0, 1.2), 0.13, 0.09, 'leaf_mid', 8)]
    for i in range(6):
        a = i / 6 * math.tau + 0.3
        d = Vector((math.cos(a), math.sin(a), 0))
        base = Vector((0, 0, 1.0 + 0.08 * i))
        tip = base + d * 1.1 + Vector((0, 0, 0.35 - (i % 2) * 0.5))
        p.append(limb('Petiole', base, base + d * 0.3 + Vector((0, 0, 0.25)), 0.035, 0.025, 'leaf_mid', 5))
        lf = blob('Leaf', 0.55, (base + tip) / 2 + Vector((0, 0, 0.1)), (1.5, 0.42, 0.06), 'banana_leaf', 1, 0.08)
        lf.rotation_euler = (0, -math.atan2(tip.z - base.z, 1.1), a); p.append(lf)
    return make('Banana', p)

def bush(name, flowers=None, n=4, scale=1.0):
    p = []
    for k in range(n):
        a = k / n * math.tau
        p.append(blob('Bush', R.uniform(0.38, 0.5) * scale, (0.35 * scale * math.cos(a), 0.3 * scale * math.sin(a), 0.32 * scale), (1, 1, 0.8), 'leaf_mid', 1, 0.2))
    p.append(blob('BushTop', 0.42 * scale, (0, 0, 0.62 * scale), (1, 1, 0.75), 'leaf_light', 1, 0.2))
    if flowers:
        for k in range(14):
            a = R.uniform(0, math.tau); z = R.uniform(0.35, 0.85) * scale
            p.append(blob('Flower', 0.09 * scale, (0.55 * scale * math.cos(a), 0.5 * scale * math.sin(a), z), (1, 1, 1), flowers, 0, 0.1)) if k % 2 == 0 else None
    return make(name, p)

def flowers():
    p = []
    cols = ['santan', 'flower_white', 'flower_yellow', 'santan', 'bougain']
    for k in range(9):
        x, y = R.uniform(-0.45, 0.45), R.uniform(-0.35, 0.35); h = R.uniform(0.18, 0.36)
        p.append(limb('Stem', (x, y, 0), (x, y, h), 0.012, 0.01, 'leaf_mid', 4))
        p.append(blob('Head', 0.06, (x, y, h), (1, 1, 0.7), cols[k % len(cols)], 0, 0.05))
        lf = blob('Leaf', 0.08, (x + 0.05, y, h * 0.4), (1.6, 0.6, 0.2), 'leaf_mid', 0, 0.05); p.append(lf)
    return make('Flowers', p)

def grass():
    p = []
    for k in range(8):
        a = k / 8 * math.tau + R.uniform(-0.3, 0.3); r = R.uniform(0.02, 0.12)
        base = Vector((r * math.cos(a), r * math.sin(a), 0)); h = R.uniform(0.25, 0.45)
        tip = base + Vector((math.cos(a) * 0.12, math.sin(a) * 0.12, h))
        p.append(limb('Blade', base, tip, 0.035, 0.002, 'leaf_light' if k % 3 else 'leaf_mid', 3))
    return make('Grass', p)

def rock(name, s):
    return make(name, [blob('Rock', 0.5 * s, (0, 0, 0.18 * s), (1.3, 1, 0.65), 'rock', 1, 0.22), blob('Moss', 0.32 * s, (0.1 * s, 0, 0.42 * s), (1.2, 1, 0.35), 'leaf_light', 1, 0.2)])

# ------------------------------------------------------------------------------------------ buildings
def tin_roof(w, d, z, m, overhang=0.35, pitch=0.45):
    """Corrugated gable roof: two slopes made of ridged strips."""
    p = []
    half = d / 2 + overhang
    L = math.hypot(half, pitch)
    ang = math.degrees(math.atan2(pitch, half))
    n = int((w + 2 * overhang) / 0.5)
    for side in (-1, 1):
        for i in range(n):
            x = -w / 2 - overhang + (i + 0.5) * (w + 2 * overhang) / n
            p.append(box('Rib', ((w + 2 * overhang) / n * 0.92, L, 0.05 + 0.03 * (i % 2)), (x, side * half / 2, z + pitch / 2), (side * ang, 0, 0), m=m, bevel=0))
    p.append(box('Ridge', (w + 2 * overhang, 0.2, 0.08), (0, 0, z + pitch + 0.02), m='tin', bevel=0.02))
    return p

def window(x, y, z, w, h, rot=0, frame='white', shutters=None):
    p = [box('Glass', (w, 0.06, h), (x, y, z), (0, 0, rot), m='glass', bevel=0), box('Frame', (w + 0.12, 0.04, h + 0.12), (x, y + 0.01, z), (0, 0, rot), m=frame, bevel=0.01),
         box('Sill', (w + 0.25, 0.18, 0.06), (x, y - 0.06, z - h / 2 - 0.05), (0, 0, rot), m=frame, bevel=0.01)]
    if shutters:
        for s in (-1, 1): p.append(box('Shutter', (w / 2 + 0.05, 0.04, h + 0.05), (x + s * (w * 0.75 + 0.05), y - 0.04, z), (0, 0, rot), m=shutters, bevel=0.01))
    return p

def house(name, wall, roof, shutters, two_storey=False):
    w, d, h = 4.2, 3.6, 2.7
    p = [box('Base', (w + 0.2, d + 0.2, 0.2), (0, 0, 0.1), m='conc', bevel=0.02), box('Walls', (w, d, h), (0, 0, 0.2 + h / 2), m=wall, bevel=0.03)]
    z = 0.2 + h
    if two_storey:   # bahay na bato style: wooden upper floor with capiz windows
        p += [box('Upper', (w + 0.3, d + 0.3, 2.3), (0, 0, z + 1.15), m='wood', bevel=0.03), box('Beam', (w + 0.4, d + 0.4, 0.12), (0, 0, z + 0.06), m='wood_dark', bevel=0.01)]
        for i in range(4):
            p += [box('Capiz', (0.75, 0.06, 1.1), (-1.5 + i, -(d + 0.3) / 2 - 0.02, z + 1.2), m='capiz', bevel=0), box('CapizFrame', (0.8, 0.04, 1.2), (-1.5 + i, -(d + 0.3) / 2 - 0.04, z + 1.2), m='wood_dark', bevel=0)]
        z += 2.3
    p += tin_roof(w, d, z, roof)
    p += window(-1.1, -d / 2 - 0.03, 1.5, 1.0, 1.0, shutters=shutters) + window(1.25, -d / 2 - 0.03, 1.5, 0.8, 1.0, shutters=shutters)
    p += [box('Door', (0.85, 0.08, 1.9), (0.1, -d / 2 - 0.03, 1.15), m='wood_dark', bevel=0.02), box('Awning', (1.4, 0.7, 0.05), (0.1, -d / 2 - 0.35, 2.3), (12, 0, 0), m=roof, bevel=0.01)]
    p += [box('Aircon', (0.6, 0.35, 0.4), (1.7, -d / 2 - 0.2, 0.55), m='white', bevel=0.03), cyl('AirconFan', 0.13, 0.02, (1.7, -d / 2 - 0.38, 0.55), (90, 0, 0), m='dark_steel', verts=14, bevel=0)]
    for k in range(3):   # potted plants along the front
        p.append(cyl('Pot', 0.16, 0.26, (-1.8 + k * 0.45, -d / 2 - 0.35, 0.33), m='tin_rust', verts=10, bevel=0.01))
        p.append(blob('PotLeaf', 0.2, (-1.8 + k * 0.45, -d / 2 - 0.35, 0.6), (1, 1, 1.2), 'leaf_mid' if k != 1 else 'bougain', 1, 0.2))
    return make(name, p)

def kubo():
    p = []
    for sx in (-1, 1):
        for sy in (-1, 1): p.append(cyl('Stilt', 0.08, 1.0, (1.1 * sx, 1.0 * sy, 0.5), m='bamboo', verts=8, bevel=0))
    p += [box('FloorK', (2.6, 2.4, 0.12), (0, 0, 1.05), m='bamboo', bevel=0.02), box('WallsK', (2.4, 2.2, 1.5), (0, 0, 1.85), m='straw', bevel=0.02)]
    for i in range(7): p.append(box('Slat', (0.05, 0.04, 1.5), (-1.05 + i * 0.35, -1.12, 1.85), m='bamboo', bevel=0))
    p.append(cone('Thatch', 2.35, 0.05, 1.7, (0, 0, 3.4), (0, 0, 45), m='straw_dark', verts=4))
    p.append(cone('ThatchTop', 2.0, 0.05, 1.45, (0, 0, 3.55), (0, 0, 45), m='straw', verts=4))
    p += [box('WindowK', (0.7, 0.06, 0.6), (0.5, -1.13, 1.95), m='black', bevel=0), box('DoorK', (0.6, 0.06, 1.1), (-0.6, -1.13, 1.65), m='wood_dark', bevel=0)]
    for k in range(4): p.append(box('Step', (0.6, 0.25, 0.05), (-0.6, -1.35 - k * 0.15, 0.85 - k * 0.25), m='bamboo', bevel=0))
    return make('Kubo', p)

def sari_sari():
    p = [box('Body', (2.6, 2.2, 2.4), (0, 0, 1.2), m='house_yellow', bevel=0.04), box('Counter', (2.0, 0.3, 0.1), (0, -1.2, 1.05), m='wood', bevel=0.01)]
    p.append(box('Opening', (2.0, 0.05, 1.0), (0, -1.11, 1.6), m='wood_dark', bevel=0))
    for i in range(9): p.append(cyl('Grille', 0.015, 1.0, (-0.9 + i * 0.225, -1.15, 1.6), m='steel', verts=6, bevel=0))
    for i in range(10):    # snack packets hanging in strips
        p.append(box('Snack', (0.12, 0.02, 0.18), (-0.9 + i * 0.2, -1.2, 2.0 - (i % 3) * 0.2), m=['red', 'yellow', 'blue', 'orange', 'pink'][i % 5], bevel=0))
    for i in range(8): p.append(box('Awn', (0.38, 1.0, 0.04), (-1.33 + i * 0.38, -1.55, 2.3), (-18, 0, 0), m='green' if i % 2 else 'white', bevel=0.005))
    p += [box('Sign', (2.2, 0.06, 0.45), (0, -1.14, 2.58), m='red', bevel=0.02), box('Bench', (1.6, 0.35, 0.06), (0, -2.0, 0.42), m='wood', bevel=0.01)]
    for sx in (-1, 1): p.append(box('BenchLeg', (0.06, 0.3, 0.4), (0.7 * sx, -2.0, 0.2), m='wood_dark', bevel=0))
    p += tin_roof(2.6, 2.2, 2.4, 'tin_rust', 0.2, 0.3)
    p.append(box('Crate', (0.5, 0.4, 0.35), (1.0, -1.6, 0.18), m='plastic_red', bevel=0.02))
    for k in range(6): p.append(cyl('Bottle', 0.035, 0.22, (0.82 + (k % 3) * 0.12, -1.53 - (k // 3) * 0.14, 0.46), m='black', verts=6, bevel=0))
    return make('SariSari', p)

# ------------------------------------------------------------------------------------------ street furniture
def power_pole():
    p = [cyl('Pole', 0.13, 7.5, (0, 0, 3.75), m='pole', verts=10, bevel=0), box('Cross', (2.2, 0.14, 0.14), (0, 0, 7.0), m='wood_dark', bevel=0.01),
         box('Cross2', (1.6, 0.12, 0.12), (0, 0, 6.4), m='wood_dark', bevel=0.01), cyl('Transformer', 0.28, 0.75, (0, 0.32, 5.6), m='tin', verts=14, bevel=0.03)]
    for x in (-0.95, -0.35, 0.35, 0.95): p.append(cyl('Insulator', 0.05, 0.14, (x, 0, 7.14), m='white', verts=8, bevel=0))
    for x in (-0.7, 0.7): p.append(cyl('Insulator', 0.05, 0.14, (x, 0, 6.53), m='white', verts=8, bevel=0))
    return make('PowerPole', p)

def lamp_post():
    p = [cyl('LPole', 0.07, 3.4, (0, 0, 1.7), m='black', verts=8, bevel=0), limb('Arm', (0, 0, 3.3), (0.6, 0, 3.45), 0.04, 0.03, 'black', 6),
         cone('Shade', 0.22, 0.06, 0.18, (0.6, 0, 3.4), m='black', verts=10), sphere('Bulb', 0.11, (0.6, 0, 3.27), m='bulb', seg=10, rings=6)]
    return make('LampPost', p)

def string_lights():
    """A sagging strand of bulbs, 6 m long, hung between two points at z=3."""
    p = []
    for k in range(13):
        x = -3 + k * 0.5; z = 3 - 0.6 * (1 - (x / 3) ** 2)
        if k < 12:
            x2 = x + 0.5; z2 = 3 - 0.6 * (1 - (x2 / 3) ** 2)
            p.append(limb('Wire', (x, 0, z), (x2, 0, z2), 0.008, 0.008, 'wire', 4))
        p.append(sphere('Bulb', 0.06, (x, 0, z - 0.08), m='bulb', seg=8, rings=5))
    return make('StringLights', p)

def parol():
    """The Christmas star lantern, a Filipino icon (it's November!)."""
    p = []
    for i in range(5):
        a = i / 5 * math.tau + math.pi / 2
        p.append(cone('Ray', 0.16, 0.02, 0.5, (0.25 * math.cos(a), 0, 0.25 * math.sin(a) + 0.6), (0, math.degrees(-a) + 90, 0), m='bulb', verts=4))
    p += [sphere('Core', 0.17, (0, 0, 0.6), m='bulb', seg=12, rings=8)]
    for k in range(3): p.append(limb('Tail', (-0.08 + k * 0.08, 0, 0.42), (-0.12 + k * 0.12, 0, -0.1), 0.015, 0.01, ['red', 'yellow', 'blue'][k], 4))
    p.append(limb('Hang', (0, 0, 0.78), (0, 0, 1.3), 0.008, 0.008, 'wire', 4))
    return make('Parol', p)

def bamboo_fence():
    p = [box('Rail', (2.0, 0.06, 0.06), (0, 0, 0.75), m='bamboo', bevel=0), box('Rail', (2.0, 0.06, 0.06), (0, 0, 0.3), m='bamboo', bevel=0)]
    for i in range(9): p.append(cyl('Post', 0.04, 1.0 + 0.08 * (i % 2), (-0.9 + i * 0.225, 0.04, 0.5), m='bamboo', verts=7, bevel=0))
    return make('Fence', p)

def hoop():
    p = [cyl('HoopPole', 0.08, 3.2, (0, 0, 1.6), m='pole', verts=8, bevel=0), box('Board', (1.2, 0.06, 0.8), (0, -0.4, 3.1), m='white', bevel=0.02),
         box('BoardBox', (0.45, 0.065, 0.35), (0, -0.405, 2.95), m='red', bevel=0), limb('HoopArm', (0, 0, 3.0), (0, -0.4, 3.0), 0.04, 0.04, 'pole', 6),
         torus('Ring', 0.22, 0.02, (0, -0.68, 2.78), m='hoop', mseg=6)]
    return make('Hoop', p)

def clothesline():
    p = [cyl('PostC', 0.05, 2.0, (-1.8, 0, 1.0), m='wood', verts=8, bevel=0), cyl('PostC', 0.05, 2.0, (1.8, 0, 1.0), m='wood', verts=8, bevel=0),
         limb('Line', (-1.8, 0, 1.9), (1.8, 0, 1.9), 0.008, 0.008, 'wire', 4)]
    for i, c in enumerate(['cloth1', 'cloth2', 'cloth3', 'flower_white', 'cloth1', 'cloth2']):
        w = R.uniform(0.35, 0.5); h = R.uniform(0.4, 0.7)
        p.append(box('Cloth', (w, 0.02, h), (-1.4 + i * 0.56, 0, 1.88 - h / 2), (0, 0, R.uniform(-8, 8)), m=c, bevel=0.01))
    return make('Clothesline', p)

def taal():
    """Taal volcano: the island-in-a-lake that every Batangueño knows. Drawn small and far away."""
    prof = [(0, 0), (7.5, 0), (6.8, 0.35), (5.0, 1.0), (3.6, 2.0), (2.6, 2.7), (1.9, 3.0), (1.5, 2.75), (0.6, 2.6), (0, 2.6)]
    v = lathe('Volcano', prof, m='volcano_green', seg=28)
    bm = bmesh.new(); bm.from_mesh(v.data)
    for vert in bm.verts:
        n = math.sin(vert.co.x * 1.3) * math.cos(vert.co.y * 1.1) * 0.18 + math.sin(vert.co.y * 2.7) * 0.08
        if vert.co.z > 0.2: vert.co.z += n
    bm.to_mesh(v.data); bm.free()
    for f in v.data.polygons: f.use_smooth = False
    top = lathe('Ash', [(0, 2.3), (2.4, 2.35), (1.95, 2.95), (1.5, 2.8), (0.6, 2.65), (0, 2.65)], m='volcano_brown', seg=28)
    return make('Taal', [v, top])

def hill():
    return make('Hill', [blob('HillBody', 6, (0, 0, -1.5), (1.6, 1.2, 0.55), 'leaf_mid', 2, 0.1), blob('HillTop', 3.2, (1.5, -0.8, 1.2), (1.5, 1.2, 0.35), 'leaf_light', 1, 0.14)])

def pendant():
    """Hanging ceiling lamp for the interiors (bulb glows at night)."""
    return make('Pendant', [limb('Cord', (0, 0, 0), (0, 0, -0.7), 0.01, 0.01, 'wire', 4), cone('PShade', 0.08, 0.3, 0.22, (0, 0, -0.78), m='capiz', verts=14),
                            sphere('Bulb', 0.09, (0, 0, -0.88), m='bulb', seg=10, rings=6)])

reset()
pieces = [acacia(), mango(), palm(), banana(), bush('Bush', None, 4, 1.0), bush('BushBig', None, 6, 1.5), bush('Bougainvillea', 'bougain', 5, 1.1),
          bush('Santan', 'santan', 4, 0.8), flowers(), grass(), rock('Rock', 1.0), rock('RockBig', 2.2),
          house('HouseMint', 'house_mint', 'tin_teal', 'wood'), house('HousePeach', 'house_peach', 'tin_rust', 'house_mint'),
          house('HouseBlue', 'house_blue', 'tin', 'wood_light'), house('HouseBato', 'house_lilac', 'tin_rust', None, True),
          kubo(), sari_sari(), power_pole(), lamp_post(), string_lights(), parol(), bamboo_fence(), hoop(), clothesline(), taal(), hill(), pendant()]
for o in pieces: o.location = (0, 0, 0)
export(OUT + 'world_kit.glb')
print('world kit tris', stats())
