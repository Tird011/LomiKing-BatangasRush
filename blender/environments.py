import bpy
import sys, math, random
import os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from lib import *
from mathutils import Vector
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'mobile', 'assets', 'models') + os.sep
R = random.Random(3)

def text(name, body, size, loc, rot=(90, 0, 0), m='white', depth=0.02, align='CENTER'):
    bpy.ops.object.text_add()
    t = bpy.context.active_object
    t.data.body = body; t.data.size = size; t.data.extrude = depth; t.data.align_x = align; t.data.align_y = 'CENTER'
    t.location = Vector(loc); t.rotation_euler = [math.radians(a) for a in rot]
    bpy.ops.object.convert(target='MESH')
    t = bpy.context.active_object; t.name = name
    t.data.materials.clear(); t.data.materials.append(mat(m))
    return t

def monobloc(name, loc, rot=0, m='monoblock'):
    p = [box('Seat', (0.42, 0.42, 0.05), (0, 0, 0.42), m=m, bevel=0.03),
         box('Back', (0.42, 0.05, 0.42), (0, 0.2, 0.68), (-8, 0, 0), m=m, bevel=0.03)]
    for sx in (-1, 1):
        for sy in (-1, 1):
            p.append(cyl('ChairLeg', 0.025, 0.42, (0.18 * sx, 0.18 * sy, 0.21), m=m, verts=10, bevel=0))
    o = join(name, p); o.location = Vector(loc); o.rotation_euler.z = math.radians(rot)
    return o

def floor_tiles(name, w, d, tile=0.6, a='tile', b='cream', z=0):
    p = [box(name + 'Base', (w, d, 0.04), (0, 0, z - 0.02), m=a, bevel=0)]
    nx, ny = int(w / tile), int(d / tile)
    for i in range(nx):
        for j in range(ny):
            if (i + j) % 2: p.append(box('T', (tile * 0.98, tile * 0.98, 0.006), (-w / 2 + tile * (i + 0.5), -d / 2 + tile * (j + 0.5), z + 0.003), m=b, bevel=0))
    return join(name, p)

def potted_plant(name, loc, s=1.0):
    p = [cyl('Pot', 0.18 * s, 0.3 * s, (0, 0, 0.15 * s), m='brown', bevel=0.02)]
    for k in range(5):
        a = k / 5 * math.tau
        p.append(sphere('Leaf', 0.16 * s, (0.1 * s * math.cos(a), 0.1 * s * math.sin(a), (0.42 + 0.06 * (k % 2)) * s), (1, 1, 1.3), m='leaf', seg=12, rings=8))
    o = join(name, p); o.location = Vector(loc); return o

# --------------------------------------------------------------------------------------------- house exterior
def lomi_house():
    reset()
    parts = []
    parts.append(box('Ground', (12, 10, 0.1), (0, 0, -0.05), m='grass', bevel=0))
    parts.append(box('Path', (2.2, 4, 0.02), (0, -3.6, 0.01), m='sidewalk', bevel=0))
    parts.append(box('Platform', (5.6, 4.2, 0.25), (0, 0, 0.12), m='sidewalk', bevel=0.03))
    # walls with a big serving window on the front
    parts += [box('WallBack', (5, 0.2, 2.6), (0, 1.9, 1.55), m='wall'),
              box('WallL', (0.2, 3.8, 2.6), (-2.5, 0, 1.55), m='wall'), box('WallR', (0.2, 3.8, 2.6), (2.5, 0, 1.55), m='wall'),
              box('FrontLow', (5, 0.2, 0.9), (0, -1.9, 0.7), m='wall2'), box('FrontTop', (5, 0.2, 0.55), (0, -1.9, 2.58), m='wall'),
              box('FrontPostL', (0.5, 0.2, 2.6), (-2.25, -1.9, 1.55), m='wall'), box('FrontPostR', (0.5, 0.2, 2.6), (2.25, -1.9, 1.55), m='wall'),
              box('Sill', (4.2, 0.5, 0.08), (0, -2.0, 1.17), m='wood', bevel=0.02)]
    # corrugated roof
    parts.append(box('Roof', (6.2, 5.0, 0.08), (0, 0.0, 3.05), (8, 0, 0), m='roof', bevel=0.02))
    for i in range(15):
        x = -2.8 + i * 0.4
        parts.append(cyl('RoofRidge', 0.05, 5.0, (x, 0.0, 3.1), (98, 0, 0), m='roof', verts=10, bevel=0))
    parts.append(box('RoofTrim', (6.3, 0.15, 0.2), (0, -2.45, 2.92), m='red', bevel=0.03))
    # striped awning over the window
    for i in range(8):
        parts.append(box('Awning', (0.55, 1.1, 0.04), (-1.92 + i * 0.55, -2.45, 2.3), (-25, 0, 0), m='awning1' if i % 2 == 0 else 'awning2', bevel=0.01))
    # sign board
    parts.append(box('Sign', (3.6, 0.15, 0.75), (0, -2.05, 3.55), m='yellow', bevel=0.06))
    parts.append(box('SignBack', (3.8, 0.1, 0.9), (0, -1.98, 3.55), m='wood_dark', bevel=0.04))
    parts.append(text('SignText', 'LOMI KING', 0.5, (0, -2.14, 3.52), m='red', depth=0.03))
    parts.append(text('SignSub', 'BATANGAS LOMIHAN', 0.16, (0, -2.13, 3.2), m='wood_dark', depth=0.01))
    for sx in (-1, 1): parts.append(cyl('SignPole', 0.05, 0.6, (1.5 * sx, -1.98, 3.05), m='wood_dark', bevel=0))
    # inside glimpses: counter + big pot
    parts.append(box('InsideCounter', (4.2, 0.6, 0.15), (0, -1.6, 1.25), m='wood_light', bevel=0.02))
    parts.append(lathe('BigPot', [(0, 0), (0.3, 0), (0.33, 0.05), (0.33, 0.45), (0.36, 0.48), (0.3, 0.48), (0.3, 0.06), (0, 0.06)], m='steel', loc=(1.2, -1.5, 1.32)))
    parts.append(cyl('Broth', 0.29, 0.02, (1.2, -1.5, 1.72), m='broth', bevel=0))
    # door on the side
    parts.append(box('Door', (0.08, 0.95, 2.0), (2.62, 0.6, 1.25), m='wood', bevel=0.02))
    parts.append(sphere('Knob', 0.05, (2.68, 0.25, 1.25), m='yellow', seg=12, rings=8))
    # bench, plants, string lights
    parts.append(box('Bench', (1.6, 0.4, 0.08), (-1.6, -3.0, 0.45), m='wood', bevel=0.02))
    for sx in (-0.7, 0.7): parts.append(box('BenchLeg', (0.08, 0.35, 0.45), (-1.6 + sx, -3.0, 0.22), m='wood_dark', bevel=0.01))
    parts.append(potted_plant('Plant1', (2.9, -2.4, 0), 1.3)); parts.append(potted_plant('Plant2', (-3.0, -1.8, 0), 1.1))
    for i in range(9):
        x = -2.4 + i * 0.6
        parts.append(sphere('Bulb', 0.06, (x, -2.6, 2.05 - 0.12 * math.sin(i / 8 * math.pi)), m=['yellow', 'red', 'green', 'blue'][i % 4], seg=10, rings=6))
    # a menu standee
    parts.append(box('Standee', (0.7, 0.05, 1.0), (1.8, -3.0, 0.75), (-10, 0, 15), m='wood_dark', bevel=0.02))
    parts.append(text('MenuText', 'LOMI\nP85', 0.18, (1.79, -3.04, 0.8), (80, 0, 15), m='yellow', depth=0.01))
    group('LomiHouse', parts)
    export(OUT + 'lomi_house.glb'); print('house', stats())

# --------------------------------------------------------------------------------------------- counter (hub room)
def counter_room():
    reset()
    p = [floor_tiles('Floor', 8.4, 9, 0.7)]
    p += [box('BackWall', (8.4, 0.2, 3.2), (0, 4.5, 1.6), m='wall'), box('WallL', (0.2, 9, 3.2), (-4.2, 0, 1.6), m='wall2'),
          box('WallR', (0.2, 9, 3.2), (4.2, 0, 1.6), m='wall2')]
    # entrance door + windows in the back wall
    p += [box('DoorFrame', (1.4, 0.1, 2.3), (0, 4.38, 1.15), m='wood_dark', bevel=0.02), box('DoorGlass', (1.1, 0.05, 1.9), (0, 4.34, 1.1), m='glass', bevel=0)]
    for sx in (-1, 1):
        p += [box('Window', (1.8, 0.08, 1.1), (2.4 * sx, 4.37, 1.7), m='glass', bevel=0), box('WindowFrame', (1.95, 0.06, 1.25), (2.4 * sx, 4.41, 1.7), m='white', bevel=0.02)]
    # the counter (foreground)
    p += [box('Counter', (6.2, 0.9, 1.05), (0, -2.6, 0.52), m='wood', bevel=0.04),
          box('CounterTop', (6.4, 1.05, 0.08), (0, -2.6, 1.09), m='wood_light', bevel=0.03),
          box('CounterPanel', (6.2, 0.04, 0.6), (0, -3.07, 0.55), m='red', bevel=0.01)]
    # cash box, chicharon jars, condiment set, napkins
    p += [box('Register', (0.6, 0.45, 0.32), (2.2, -2.6, 1.3), m='dark_steel', bevel=0.04), box('RegisterScreen', (0.4, 0.05, 0.2), (2.2, -2.8, 1.5), (-20, 0, 0), m='green', bevel=0.01)]
    for i, x in enumerate((-2.6, -2.15)):
        p.append(cyl('Jar', 0.17, 0.4, (x, -2.5, 1.33), m='glass', bevel=0.02))
        p.append(cyl('JarLid', 0.18, 0.06, (x, -2.5, 1.56), m='red', bevel=0.01))
        for k in range(5): p.append(sphere('Chicharon', 0.07, (x + R.uniform(-0.08, 0.08), -2.5 + R.uniform(-0.08, 0.08), 1.2 + k * 0.06), (1.3, 1, 0.7), m='chicharon', seg=8, rings=6))
    for i, (c, h) in enumerate((('black', 0.3), ('orange', 0.26), ('glass', 0.24))):
        p.append(cyl('Bottle', 0.05, h, (-0.4 + i * 0.14, -2.5, 1.13 + h / 2), m=c, verts=12, bevel=0.01))
    p.append(box('Napkins', (0.25, 0.15, 0.18), (0.25, -2.5, 1.22), m='white', bevel=0.02))
    # menu board on the left wall
    p += [box('MenuBoard', (0.08, 2.6, 1.4), (-4.05, -0.5, 2.1), m='black', bevel=0.03), box('MenuFrame', (0.06, 2.75, 1.55), (-4.1, -0.5, 2.1), m='wood', bevel=0.02)]
    for i, line in enumerate(['LOMI BATANGAS   85', 'LOMI OVERLOAD  130', 'TAPSILOG       110', 'CHICKEN CHAMI   95', 'SHANGHAI        60']):
        p.append(text('MenuLine', line, 0.13, (-3.99, -0.5, 2.6 - i * 0.24), (90, 0, 90), m='yellow', depth=0.005))
    # queue rope posts
    for k in range(4):
        p.append(cyl('Post', 0.05, 0.9, (1.4, -1.2 + k * 1.1, 0.45), m='steel', verts=12, bevel=0))
        p.append(cyl('PostBase', 0.18, 0.05, (1.4, -1.2 + k * 1.1, 0.03), m='steel', verts=16, bevel=0))
    # wall fan + calendar + plant
    p += [cyl('Fan', 0.35, 0.12, (3.9, 2.5, 2.6), (0, 90, 0), m='blue', bevel=0.02), potted_plant('Plant', (-3.5, 3.6, 0), 1.2)]
    p += [box('Calendar', (0.05, 0.6, 0.8), (4.08, 0.5, 2.0), m='white', bevel=0.01), box('CalendarTop', (0.06, 0.62, 0.2), (4.08, 0.5, 2.35), m='red', bevel=0.01)]
    group('CounterRoom', p)
    export(OUT + 'counter.glb'); print('counter', stats())

# --------------------------------------------------------------------------------------------- kitchen
def kitchen_room():
    reset()
    p = [floor_tiles('Floor', 7, 5, 0.5, 'tile', 'white')]
    p += [box('BackWall', (7, 0.2, 3), (0, 1.6, 1.5), m='white')]
    for i in range(14):           # tiled backsplash
        for j in range(4):
            if (i + j) % 2: p.append(box('Splash', (0.48, 0.02, 0.28), (-3.25 + i * 0.5, 1.49, 1.1 + j * 0.3), m='teal', bevel=0))
    p += [box('Bench', (6.6, 1.4, 0.9), (0, 0.8, 0.45), m='wood', bevel=0.03), box('BenchTop', (6.8, 1.5, 0.08), (0, 0.8, 0.94), m='steel', bevel=0.02)]
    # stove with two burners
    p += [box('Stove', (1.9, 0.9, 0.22), (0.9, 0.8, 1.09), m='teal', bevel=0.05), box('StoveTop', (1.84, 0.84, 0.03), (0.9, 0.8, 1.21), m='dark_steel', bevel=0.01)]
    for bx in (0.45, 1.35):
        p.append(torus('Grate', 0.25, 0.025, (bx, 0.8, 1.25), m='black', mseg=6))
        for a in range(4): p.append(box('GrateBar', (0.52, 0.04, 0.03), (bx, 0.8, 1.26), (0, 0, 45 * a), m='black', bevel=0))
        p.append(cyl('Knob', 0.06, 0.07, (bx, 0.33, 1.09), (90, 0, 0), m='white', verts=12, bevel=0.01))
    # kawali on the left burner (separate object so it can shake)
    kaw = lathe('Kawali', [(0, 0.0), (0.17, 0.015), (0.34, 0.11), (0.42, 0.21), (0.44, 0.23), (0.41, 0.21), (0.33, 0.13), (0.16, 0.045), (0, 0.04)], m='wok', loc=(0.45, 0.8, 1.27))
    for s in (-1, 1): torus('KawaliHandle', 0.07, 0.02, (0.47 * s, 0, 0.21), m='wood_dark', mseg=6).parent = kaw
    # stock pot on the right burner
    p.append(lathe('StockPot', [(0, 0), (0.34, 0), (0.36, 0.05), (0.36, 0.55), (0.39, 0.58), (0.33, 0.58), (0.33, 0.06), (0, 0.06)], m='steel', loc=(1.35, 0.8, 1.27)))
    p.append(cyl('PotBroth', 0.33, 0.02, (1.35, 0.8, 1.76), m='broth', bevel=0))
    # chopping board + knife
    board = box('ChoppingBoard', (1.1, 0.7, 0.06), (-1.3, 0.6, 1.01), m='wood_light', bevel=0.02)
    knife = join('Knife', [box('Blade', (0.42, 0.1, 0.012), (0, 0, 0), m='steel', bevel=0.004), box('Handle', (0.2, 0.06, 0.04), (-0.3, 0, 0), m='black', bevel=0.01)])
    knife.location = (-1.0, 0.45, 1.07)
    # ingredient shelf: bottles and jars
    p += [box('Shelf', (3, 0.3, 0.05), (-1.5, 1.35, 2.15), m='wood', bevel=0.01)]
    shelf_items = [('Toyo', 'black', 0.32), ('Patis', 'orange', 0.3), ('Suka', 'glass', 0.3), ('Oyster', 'brown', 0.22)]
    for i, (n, c, h) in enumerate(shelf_items):
        p.append(cyl(n, 0.07, h, (-2.7 + i * 0.3, 1.35, 2.18 + h / 2), m=c, verts=14, bevel=0.01))
    for i, c in enumerate(('white', 'egg', 'red', 'leaf')):
        p.append(cyl('SpiceJar', 0.09, 0.18, (-1.4 + i * 0.32, 1.35, 2.27), m='glass', verts=14, bevel=0.01))
        p.append(cyl('SpiceLid', 0.095, 0.05, (-1.4 + i * 0.32, 1.35, 2.38), m=c, verts=14, bevel=0.01))
    # hanging utensils
    p.append(cyl('Rail', 0.02, 2.2, (1.2, 1.45, 2.4), (0, 90, 0), m='steel', verts=8, bevel=0))
    for i in range(4):
        x = 0.4 + i * 0.5
        p.append(cyl('UtensilHandle', 0.02, 0.45, (x, 1.43, 2.15), m='wood_dark', verts=8, bevel=0))
        p.append(sphere('Ladle', 0.09, (x, 1.43, 1.9), (1, 1, 0.5), m='steel', seg=12, rings=8))
    # rice cooker + egg tray + bowls stack
    p += [cyl('RiceCooker', 0.28, 0.35, (2.7, 0.9, 1.16), m='pink', bevel=0.06), sphere('RiceLid', 0.27, (2.7, 0.9, 1.34), (1, 1, 0.4), m='white', seg=20, rings=10)]
    p.append(box('EggTray', (0.5, 0.35, 0.05), (-2.6, 0.5, 1.0), m='wood_light', bevel=0.01))
    for i in range(3):
        for j in range(2): p.append(sphere('Egg', 0.06, (-2.75 + i * 0.15, 0.42 + j * 0.15, 1.08), (1, 1, 1.3), m='cream', seg=12, rings=8))
    for k in range(3): p.append(lathe('BowlStack', [(0, 0), (0.12, 0), (0.22, 0.12), (0.2, 0.12), (0, 0.02)], m='white', loc=(2.1, 1.1, 0.98 + k * 0.05)))
    group('Kitchen', p + [kaw, board, knife])
    export(OUT + 'kitchen.glb'); print('kitchen', stats())

# --------------------------------------------------------------------------------------------- diner
TABLES = [(-2.0, 0.6), (2.0, 0.6), (-2.0, 3.0), (2.0, 3.0)]
def diner_room():
    reset()
    p = [floor_tiles('Floor', 9, 8, 0.75, 'floor', 'cream')]
    p += [box('BackWall', (9, 0.2, 3), (0, 4.6, 1.5), m='wall'), box('WallL', (0.2, 8, 3), (-4.5, 0.6, 1.5), m='wall2'), box('WallR', (0.2, 8, 3), (4.5, 0.6, 1.5), m='wall2')]
    for i in range(3): p += [box('Window', (1.6, 0.08, 1.0), (-3 + i * 3, 4.48, 1.8), m='glass', bevel=0), box('WinFrame', (1.75, 0.06, 1.15), (-3 + i * 3, 4.52, 1.8), m='white', bevel=0.02)]
    tables = []
    for n, (x, y) in enumerate(TABLES, 1):
        t = [cyl('Top', 0.6, 0.06, (0, 0, 0.76), m='wood_light', bevel=0.02), cyl('Leg', 0.06, 0.74, (0, 0, 0.37), m='dark_steel', verts=12, bevel=0),
             cyl('Foot', 0.32, 0.04, (0, 0, 0.02), m='dark_steel', bevel=0.01),
             cyl('Holder', 0.07, 0.18, (0.25, 0.1, 0.88), m='glass', verts=12, bevel=0.01), cyl('Holder2', 0.06, 0.16, (0.38, 0.1, 0.87), m='black', verts=12, bevel=0.01)]
        tt = join(f'Table{n}', t); tt.location = (x, y, 0)
        tables.append(tt)
        tables.append(monobloc(f'Chair{n}a', (x, y - 0.85, 0), 0, 'monoblock'))
        tables.append(monobloc(f'Chair{n}b', (x, y + 0.85, 0), 180, 'plastic_red'))
    # wash station on the left wall + dish rack
    p += [box('Sink', (0.9, 1.6, 0.9), (-3.95, -2.2, 0.45), m='steel', bevel=0.03), box('SinkBasin', (0.6, 1.0, 0.08), (-3.95, -2.2, 0.88), m='dark_steel', bevel=0.02),
          cyl('Faucet', 0.03, 0.4, (-4.25, -2.2, 1.1), m='steel', verts=8, bevel=0)]
    rack = [box('Rack', (0.5, 0.9, 0.05), (0, 0, 0), m='steel', bevel=0.01)]
    for k in range(5): rack.append(cyl('Plate', 0.18, 0.02, (0, -0.35 + k * 0.17, 0.18), (90, 0, 0), m='white', bevel=0.005))
    rk = join('DishRack', rack); rk.location = (-3.95, -0.9, 0.92)
    # walis tingting + dustpan by the wall
    broom = join('Walis', [cyl('Stick', 0.025, 1.3, (0, 0, 0.65), m='wood_light', verts=8, bevel=0), cone('Bristles', 0.05, 0.22, 0.5, (0, 0, 0.2), (180, 0, 0), m='wood', verts=16)])
    broom.location = (4.25, -2.6, 0.05); broom.rotation_euler.x = math.radians(-12)
    # TV on the wall + fan + plants
    p += [box('TV', (1.3, 0.1, 0.8), (0, 4.45, 2.3), m='black', bevel=0.03), potted_plant('Plant', (3.9, 4.0, 0), 1.3), potted_plant('Plant2', (-3.9, 4.0, 0), 1.1)]
    group('Diner', p + tables + [rk, broom])
    export(OUT + 'diner.glb'); print('diner', stats())

# --------------------------------------------------------------------------------------------- delivery hub + scooter
def scooter_parts():
    s = [torus('WheelF', 0.26, 0.09, (0, -0.85, 0.3), (0, 90, 0), m='black', mseg=10), torus('WheelR', 0.26, 0.09, (0, 0.65, 0.3), (0, 90, 0), m='black', mseg=10),
         cyl('HubF', 0.14, 0.12, (0, -0.85, 0.3), (0, 90, 0), m='steel', bevel=0.01), cyl('HubR', 0.14, 0.12, (0, 0.65, 0.3), (0, 90, 0), m='steel', bevel=0.01),
         box('Floorboard', (0.45, 0.9, 0.1), (0, -0.1, 0.42), m='dark_steel', bevel=0.04),
         box('Body', (0.5, 0.75, 0.45), (0, 0.5, 0.72), m='red', bevel=0.15), box('Seat', (0.38, 0.6, 0.1), (0, 0.45, 1.0), m='black', bevel=0.05),
         box('Front', (0.45, 0.2, 0.9), (0, -0.75, 0.8), (-15, 0, 0), m='red', bevel=0.08),
         cyl('Bar', 0.03, 0.8, (0, -0.85, 1.28), (0, 90, 0), m='dark_steel', verts=8, bevel=0),
         sphere('Light', 0.1, (0, -0.92, 1.12), m='yellow', seg=12, rings=8),
         box('DeliveryBox', (0.55, 0.55, 0.5), (0, 0.85, 1.3), m='yellow', bevel=0.05),
         box('BoxLid', (0.58, 0.58, 0.06), (0, 0.85, 1.57), m='red', bevel=0.02)]
    s.append(text('BoxLogo', 'LOMI', 0.14, (0, 0.85 - 0.28, 1.33), (90, 0, 0), m='red', depth=0.01))
    return s

def hub_room():
    reset()
    p = [box('Ground', (10, 8, 0.1), (0, 0, -0.05), m='sidewalk', bevel=0), box('Wall', (10, 0.3, 3), (0, 3.2, 1.5), m='wall2')]
    sc = join('Scooter', scooter_parts()); sc.location = (-1.8, -0.3, 0); sc.rotation_euler.z = math.radians(-70)
    # market stall
    p += [box('Stall', (3, 1.2, 0.9), (2, 1.6, 0.45), m='wood', bevel=0.03), box('StallTop', (3.1, 1.3, 0.06), (2, 1.6, 0.93), m='wood_light', bevel=0.02)]
    for sx in (-1, 1): p.append(cyl('StallPole', 0.05, 2.4, (2 + 1.45 * sx, 1.0, 1.2), m='wood_dark', verts=10, bevel=0))
    for i in range(6): p.append(box('Canopy', (0.55, 1.5, 0.04), (0.6 + i * 0.55, 1.5, 2.45), (-12, 0, 0), m='awning1' if i % 2 == 0 else 'yellow', bevel=0.01))
    crates = [('green', 'cabbage'), ('orange', 'carrot'), ('red', 'chili'), ('cream', 'egg'), ('pink', 'meat')]
    for i, (c, n) in enumerate(crates):
        x = 0.85 + i * 0.58
        p.append(box('Crate', (0.5, 0.45, 0.22), (x, 1.5, 1.07), m='wood_light', bevel=0.02))
        for k in range(5): p.append(sphere(n, 0.08, (x + R.uniform(-0.15, 0.15), 1.5 + R.uniform(-0.12, 0.12), 1.2), (1, 1, 0.8), m=c, seg=10, rings=6))
    p.append(box('MarketSign', (2.2, 0.08, 0.45), (2, 0.85, 2.15), m='white', bevel=0.03))
    p.append(text('MarketText', 'PALENGKE', 0.26, (2, 0.8, 2.13), m='green', depth=0.01))
    # stacked sacks + gas tank
    for k in range(3): p.append(sphere('Sack', 0.4, (-3.8 + k * 0.3, 2.5, 0.3 + k * 0.05), (1.2, 0.8, 0.7), m='cream', seg=16, rings=10))
    p.append(cyl('GasTank', 0.25, 0.8, (-2.8, 2.6, 0.4), m='red', bevel=0.08))
    p.append(text('HubText', 'DELIVERY', 0.3, (-1.8, 3.03, 2.2), m='blue', depth=0.02))
    group('DeliveryHub', p + [sc])
    export(OUT + 'hub.glb'); print('hub', stats())

def scooter_only():
    reset(); join('Scooter', scooter_parts()); export(OUT + 'scooter.glb')

# --------------------------------------------------------------------------------------------- road + obstacles
def road():
    reset()
    L = 30.0
    p = [box('Asphalt', (6.6, L, 0.1), (0, -L / 2, -0.05), m='asphalt', bevel=0)]
    for lx in (-1.1, 1.1):
        for k in range(10): p.append(box('Dash', (0.12, 1.4, 0.012), (lx, -1.5 - k * 3, 0.006), m='line', bevel=0))
    for sx in (-1, 1):
        p.append(box('Curb', (0.3, L, 0.2), (3.45 * sx, -L / 2, 0.1), m='white', bevel=0.02))
        p.append(box('Sidewalk', (2.0, L, 0.16), (4.6 * sx, -L / 2, 0.08), m='sidewalk', bevel=0))
        p.append(box('Grass', (8, L, 0.1), (9.6 * sx, -L / 2, -0.02), m='grass', bevel=0))
        for k in range(3):   # coconut trees + sari-sari stores
            y = -5 - k * 10
            tr = [cyl('Trunk', 0.14, 3.2, (0, 0, 1.6), (6 * sx, 0, 0), m='wood', verts=10, bevel=0)]
            for a in range(6):
                ang = a / 6 * math.tau
                tr.append(sphere('Frond', 0.5, (0.55 * math.cos(ang) + 0.35 * sx, 0.55 * math.sin(ang), 3.2), (1.6, 0.4, 0.15), (0, -20, math.degrees(ang)), m='leaf', seg=10, rings=6))
            t = join('Palm', tr); t.location = (6.2 * sx, y, 0)
            st = join('Store', [box('StoreBody', (2.2, 2.2, 2.2), (0, 0, 1.1), m=['wall', 'pink', 'glass'][k], bevel=0.05),
                                box('StoreRoof', (2.6, 2.6, 0.15), (0, 0, 2.3), m=['red', 'blue', 'green'][k], bevel=0.04),
                                box('StoreWin', (0.08, 1.4, 0.8), (-1.11 * sx, 0, 1.2), m='black', bevel=0)])
            st.location = (8.8 * sx, y - 5, 0)
    group('RoadSegment', p)
    export(OUT + 'road.glb'); print('road', stats())

def obstacles():
    # JEEPNEY
    reset()
    j = [box('Body', (1.6, 3.6, 1.3), (0, 0, 1.15), m='jeep_body', bevel=0.12), box('Hood', (1.4, 1.1, 0.7), (0, -2.2, 0.85), m='jeep_body', bevel=0.12),
         box('Roof', (1.7, 3.8, 0.12), (0, 0, 1.85), m='jeep_red', bevel=0.04), box('Stripe', (1.62, 3.4, 0.18), (0, 0, 1.05), m='jeep_blue', bevel=0.02),
         box('Windshield', (1.3, 0.08, 0.5), (0, -1.79, 1.45), (12, 0, 0), m='glass', bevel=0.02), box('Grill', (1.0, 0.05, 0.4), (0, -2.77, 0.9), m='steel', bevel=0.02)]
    for sx in (-1, 1):
        for k in range(3): j.append(box('Win', (0.05, 0.8, 0.4), (0.81 * sx, -0.9 + k * 1.0, 1.45), m='black', bevel=0))
        for wy in (-1.9, 1.2): j.append(cyl('Wheel', 0.36, 0.25, (0.72 * sx, wy, 0.36), (0, 90, 0), m='black', bevel=0.05))
        j.append(sphere('Headlight', 0.13, (0.5 * sx, -2.77, 1.05), m='yellow', seg=12, rings=8))
    j.append(cone('Horse', 0.08, 0.02, 0.35, (0, -2.6, 1.35), m='steel', verts=10))
    join('Jeepney', j); export(OUT + 'jeepney.glb')
    # TRICYCLE
    reset()
    t = [box('Sidecar', (1.0, 1.4, 1.2), (0.55, 0, 0.95), m='trike', bevel=0.15), box('SideRoof', (1.1, 1.5, 0.1), (0.55, 0, 1.62), m='yellow', bevel=0.03),
         box('SideWin', (0.05, 0.9, 0.5), (1.06, 0, 1.15), m='black', bevel=0), box('Moto', (0.35, 1.2, 0.45), (-0.3, 0, 0.65), m='red', bevel=0.1),
         box('Seat', (0.3, 0.5, 0.1), (-0.3, 0.2, 0.92), m='black', bevel=0.03)]
    for p_ in ((-0.3, -0.75), (-0.3, 0.6), (0.9, 0.0)): t.append(cyl('Wheel', 0.28, 0.18, (p_[0], p_[1], 0.28), (0, 90, 0), m='black', bevel=0.04))
    join('Tricycle', t); export(OUT + 'tricycle.glb')
    # POTHOLE
    reset()
    ph = [cyl('Hole', 0.7, 0.04, (0, 0, 0.0), m='black', verts=12, bevel=0, scale=(1, 0.75, 1))]
    for k in range(7):
        a = k / 7 * math.tau
        ph.append(box('Chunk', (0.18, 0.14, 0.08), (0.75 * math.cos(a), 0.56 * math.sin(a), 0.03), (R.uniform(0, 30), 0, math.degrees(a)), m='asphalt', bevel=0.02))
    ph.append(box('Puddle', (0.6, 0.4, 0.01), (0, 0, 0.03), m='glass', bevel=0.0))
    join('Pothole', ph); export(OUT + 'pothole.glb')
    # ASO (dog)
    reset()
    d = [capsule('DogBody', 0.22, 0.5, (0, 0.3, 0.45), (0, -0.25, 0.45), m='dog'), sphere('DogHead', 0.22, (0, -0.48, 0.68), m='dog', seg=20, rings=12),
         sphere('Snout', 0.11, (0, -0.68, 0.63), (1, 1.2, 0.8), m='wood_light', seg=12, rings=8), sphere('Nose', 0.04, (0, -0.8, 0.66), m='black', seg=8, rings=6)]
    for sx in (-1, 1):
        d.append(sphere('Eye', 0.035, (0.09 * sx, -0.66, 0.75), m='black', seg=8, rings=6))
        d.append(sphere('EarD', 0.09, (0.15 * sx, -0.45, 0.88), (0.6, 0.4, 1.2), (0, 30 * sx, 0), m='brown', seg=10, rings=6))
        for ly in (-0.2, 0.3): d.append(capsule('DogLeg', 0.07, 0.3, (0.13 * sx, ly, 0.05), (0.13 * sx, ly, 0.38), m='dog'))
    d.append(capsule('Tail', 0.05, 0.3, (0, 0.5, 0.55), (0, 0.75, 0.85), m='dog'))
    join('Aso', d); export(OUT + 'aso.glb')
    # CHICKEN
    reset()
    c = [sphere('ChickBody', 0.25, (0, 0, 0.45), (0.9, 1.2, 1), m='chicken', seg=20, rings=12), sphere('ChickHead', 0.14, (0, -0.25, 0.72), m='chicken', seg=16, rings=10),
         cone('Beak', 0.05, 0.0, 0.12, (0, -0.4, 0.7), (90, 0, 0), m='orange', verts=10), sphere('Comb', 0.06, (0, -0.25, 0.86), (0.5, 1.3, 1), m='comb', seg=10, rings=6),
         sphere('Tailf', 0.15, (0, 0.3, 0.6), (0.4, 0.8, 1.2), (30, 0, 0), m='brown', seg=12, rings=8)]
    for sx in (-1, 1):
        c.append(sphere('ChickEye', 0.025, (0.08 * sx, -0.36, 0.76), m='black', seg=8, rings=6))
        c.append(cyl('ChickLeg', 0.02, 0.25, (0.08 * sx, 0, 0.12), m='orange', verts=8, bevel=0))
    join('Manok', c); export(OUT + 'chicken.glb')

# --------------------------------------------------------------------------------------------- food props
def food_props():
    reset()
    items = []
    # lomi bowl
    bowl = [lathe('BowlOuter', [(0, 0.0), (0.16, 0.0), (0.18, 0.03), (0.35, 0.2), (0.4, 0.3), (0.42, 0.32), (0.38, 0.32), (0.34, 0.24), (0.15, 0.06), (0, 0.06)], m='white')]
    bowl.append(torus('BowlRim', 0.4, 0.016, (0, 0, 0.315), m='blue', mseg=6))
    bowl.append(cyl('Soup', 0.36, 0.02, (0, 0, 0.26), m='broth', bevel=0))
    for k in range(9):
        a = R.uniform(0, math.tau); r_ = R.uniform(0.05, 0.25)
        bowl.append(torus('Noodle', R.uniform(0.06, 0.1), 0.018, (r_ * math.cos(a), r_ * math.sin(a), 0.275), (R.uniform(-15, 15), R.uniform(-15, 15), 0), m='noodle', mseg=6, seg=16))
    for (n, c, s_) in (('Kikiam', 'kikiam', (1, 1, 0.4)), ('Liver', 'liver', (1.3, 0.8, 0.4)), ('Egg', 'egg', (1.4, 1, 0.3)), ('Chicharon', 'chicharon', (1, 1, 0.6))):
        for k in range(2):
            a = R.uniform(0, math.tau); r_ = R.uniform(0.08, 0.25)
            bowl.append(sphere(n, 0.06, (r_ * math.cos(a), r_ * math.sin(a), 0.29), s_, m=c, seg=10, rings=6))
    for k in range(8): bowl.append(sphere('Onion', 0.018, (R.uniform(-0.22, 0.22), R.uniform(-0.22, 0.22), 0.3), m='green', seg=6, rings=4))
    items.append(join('LomiBowl', bowl))
    # tapsilog plate
    pl = [lathe('Plate', [(0, 0), (0.3, 0), (0.42, 0.05), (0.44, 0.06), (0.4, 0.06), (0.3, 0.025), (0, 0.025)], m='white'),
          sphere('Rice', 0.17, (-0.1, 0.05, 0.08), (1, 1, 0.6), m='cream', seg=16, rings=10),
          sphere('EggWhite', 0.13, (0.15, 0.1, 0.04), (1, 1, 0.15), m='white', seg=16, rings=8), sphere('Yolk', 0.05, (0.15, 0.1, 0.06), (1, 1, 0.5), m='egg', seg=12, rings=8)]
    for k in range(4): pl.append(box('Tapa', (0.14, 0.08, 0.025), (0.05 + 0.05 * k, -0.15, 0.05 + 0.01 * k), (0, 0, R.uniform(-30, 30)), m='brown', bevel=0.01))
    t = join('TapsilogPlate', pl); t.location.x = 1.0; items.append(t)
    # chami plate
    ch = [lathe('Plate', [(0, 0), (0.3, 0), (0.42, 0.05), (0.44, 0.06), (0.4, 0.06), (0.3, 0.025), (0, 0.025)], m='white')]
    for k in range(14): ch.append(torus('ChamiNoodle', R.uniform(0.05, 0.09), 0.016, (R.uniform(-0.2, 0.2), R.uniform(-0.2, 0.2), 0.05 + R.uniform(0, 0.05)), (R.uniform(-40, 40), R.uniform(-40, 40), 0), m='orange', mseg=6, seg=14))
    for k in range(6): ch.append(box('Veg', (0.08, 0.05, 0.02), (R.uniform(-0.2, 0.2), R.uniform(-0.2, 0.2), 0.1), (0, 0, R.uniform(0, 90)), m=['green', 'orange'][k % 2], bevel=0.005))
    c_ = join('ChamiPlate', ch); c_.location.x = 2.0; items.append(c_)
    # shanghai plate
    sh = [lathe('Plate', [(0, 0), (0.3, 0), (0.42, 0.05), (0.44, 0.06), (0.4, 0.06), (0.3, 0.025), (0, 0.025)], m='white')]
    for k in range(5): sh.append(capsule('Lumpia', 0.035, 0.22, (-0.12 + k * 0.06, -0.12, 0.06), (-0.12 + k * 0.06, 0.12, 0.06), m='chicharon'))
    sh.append(cyl('Dip', 0.07, 0.05, (0.25, 0.2, 0.05), m='red', bevel=0.01))
    s_ = join('ShanghaiPlate', sh); s_.location.x = 3.0; items.append(s_)
    # pamaypay (fan) + dirty plate + bubbles for washing
    fan = join('Pamaypay', [cyl('FanFace', 0.3, 0.02, (0, 0, 0), m='wood_light', bevel=0.005), torus('FanRim', 0.3, 0.02, (0, 0, 0), m='red', mseg=6),
                            cyl('FanHandle', 0.025, 0.35, (0, -0.45, 0), (90, 0, 0), m='wood_dark', verts=8, bevel=0)])
    fan.location.x = 4.0; items.append(fan)
    dp = join('DirtyPlate', [lathe('Plate', [(0, 0), (0.3, 0), (0.42, 0.05), (0.44, 0.06), (0.4, 0.06), (0.3, 0.025), (0, 0.025)], m='white'),
                             sphere('Crumbs', 0.12, (0.05, 0, 0.03), (1.3, 1, 0.15), m='broth', seg=10, rings=6)])
    dp.location.x = 5.0; items.append(dp)
    export(OUT + 'food.glb'); print('food', stats())

if __name__ == '__main__':
    which = (sys.argv[sys.argv.index('--') + 1:] if '--' in sys.argv else []) or ['house', 'counter', 'kitchen', 'diner', 'hub', 'scooter', 'road', 'obstacles', 'food']
    for w in which:
        {'house': lomi_house, 'counter': counter_room, 'kitchen': kitchen_room, 'diner': diner_room, 'hub': hub_room,
         'scooter': scooter_only, 'road': road, 'obstacles': obstacles, 'food': food_props}[w]()
