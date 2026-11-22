"""Tiny modeling helpers on top of bpy for LomiKing's chunky, toon-friendly props."""
import bpy
import bmesh, math
from mathutils import Vector, Matrix, Euler

PALETTE = {
    'skin': (232,168,103), 'hair': (30,26,28), 'shirt': (250,250,246), 'apron': (229,72,77), 'apron_trim': (255,210,90),
    'pants': (62,82,135), 'tsinelas': (70,170,200), 'mouth': (120,20,20),
    'white': (250,250,246), 'cream': (255,240,205), 'wood': (176,116,66), 'wood_dark': (120,78,44), 'wood_light': (214,168,112),
    'steel': (176,184,192), 'dark_steel': (62,64,72), 'black': (28,28,32), 'wok': (52,52,60),
    'teal': (80,184,172), 'red': (220,64,64), 'yellow': (255,214,80), 'orange': (250,140,40), 'blue': (66,120,210),
    'green': (96,180,80), 'leaf': (70,150,70), 'pink': (250,170,180), 'brown': (150,90,50),
    'roof': (150,160,170), 'wall': (250,236,210), 'wall2': (240,200,150), 'floor': (214,196,170), 'tile': (236,226,210),
    'broth': (176,112,52), 'noodle': (246,214,150), 'egg': (255,220,90), 'kikiam': (170,86,48), 'liver': (96,46,34),
    'chicharon': (232,170,80), 'calamansi': (120,180,60), 'chili': (210,40,40), 'asphalt': (88,90,98), 'line': (250,240,200),
    'grass': (120,190,90), 'sidewalk': (200,196,186), 'jeep_body': (220,220,228), 'jeep_red': (210,50,60), 'jeep_blue': (40,90,190),
    'trike': (40,140,200), 'dog': (200,150,90), 'chicken': (245,240,230), 'comb': (220,40,40), 'glass': (170,210,235),
    'plastic_red': (210,60,60), 'mint': (120,210,170), 'lavender': (170,140,220), 'onion': (190,120,170), 'onion_in': (240,220,235),
    'cabbage': (150,205,110), 'chef': (248,248,250), 'helmet': (60,110,210), 'jacket': (245,130,40), 'glove': (255,214,80),
    'leaf_dark': (40,104,72), 'leaf_mid': (82,156,70), 'leaf_light': (160,198,66), 'leaf_sun': (214,222,96), 'trunk': (112,74,52),
    'palm_trunk': (150,118,88), 'straw': (214,178,108), 'straw_dark': (170,130,80), 'coconut': (110,130,50), 'banana_leaf': (106,186,82),
    'bougain': (226,64,150), 'santan': (240,96,58), 'flower_white': (250,248,236), 'flower_yellow': (255,206,60), 'rock': (146,146,184),
    'rock_dark': (104,104,146), 'tin': (172,178,190), 'tin_rust': (182,110,78), 'tin_teal': (80,150,150), 'house_mint': (172,222,198),
    'house_peach': (250,202,170), 'house_blue': (164,202,236), 'house_yellow': (250,226,150), 'house_lilac': (206,190,236), 'conc': (204,198,192),
    'pole': (138,128,118), 'wire': (40,40,46), 'hoop': (240,110,40), 'bamboo': (196,170,92), 'volcano_green': (104,158,92),
    'volcano_brown': (150,122,98), 'bulb': (255,224,150), 'cloth1': (240,120,120), 'cloth2': (120,170,240), 'cloth3': (250,220,110),
    'neon': (255,90,170), 'capiz': (250,240,215), 'flag1': (229,72,77), 'flag2': (255,214,80), 'flag3': (66,120,210), 'flag4': (96,180,80), 'plastic_blue': (60,120,200), 'monoblock': (240,240,240), 'awning1': (229,72,77), 'awning2': (255,250,240),
}

_mats = {}
def mat(name, rough=0.8, metal=0.0):
    key = (name, rough, metal)
    if key in _mats: return _mats[key]
    m = bpy.data.materials.new(name if (rough, metal) == (0.8, 0.0) else f"{name}_{rough}_{metal}")
    m.use_nodes = True
    b = m.node_tree.nodes.get('Principled BSDF')
    r, g, bl = [c / 255 for c in PALETTE[name]]
    lin = lambda c: c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4
    b.inputs['Base Color'].default_value = (lin(r), lin(g), lin(bl), 1)
    b.inputs['Roughness'].default_value = rough
    b.inputs['Metallic'].default_value = metal
    _mats[key] = m
    return m

def reset():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    _mats.clear()

def _finish(obj, name, material, smooth=True, bevel=0, bevel_segments=3, subsurf=0):
    obj.name = name
    obj.data.name = name
    if material is not None:
        obj.data.materials.clear(); obj.data.materials.append(material)
    if bevel:
        m = obj.modifiers.new('bevel', 'BEVEL'); m.width = bevel; m.segments = bevel_segments; m.limit_method = 'ANGLE'
    if subsurf:
        m = obj.modifiers.new('subsurf', 'SUBSURF'); m.levels = subsurf; m.render_levels = subsurf
    if smooth:
        for p in obj.data.polygons: p.use_smooth = True
        if bevel or not subsurf:
            m = obj.modifiers.new('wn', 'WEIGHTED_NORMAL'); m.keep_sharp = True
    return obj

def _xf(obj, loc=(0,0,0), rot=(0,0,0), scale=(1,1,1)):
    obj.location = Vector(loc); obj.rotation_euler = Euler([math.radians(a) for a in rot]); obj.scale = Vector(scale)
    return obj

# Blender is Z-up; we model Z-up and the glTF exporter converts to Y-up for three.js.
def box(name, size, loc=(0,0,0), rot=(0,0,0), m='white', bevel=0.02, smooth=True):
    bpy.ops.mesh.primitive_cube_add(size=1)
    o = bpy.context.active_object
    _xf(o, loc, rot, size)
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    return _finish(o, name, mat(m) if isinstance(m, str) else m, smooth, bevel)

def cyl(name, r, h, loc=(0,0,0), rot=(0,0,0), m='white', verts=32, bevel=0.01, scale=(1,1,1)):
    bpy.ops.mesh.primitive_cylinder_add(vertices=verts, radius=r, depth=h)
    o = bpy.context.active_object
    _xf(o, loc, rot, scale)
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    return _finish(o, name, mat(m) if isinstance(m, str) else m, True, bevel)

def cone(name, r1, r2, h, loc=(0,0,0), rot=(0,0,0), m='white', verts=24, bevel=0.0):
    bpy.ops.mesh.primitive_cone_add(vertices=verts, radius1=r1, radius2=r2, depth=h)
    o = bpy.context.active_object
    _xf(o, loc, rot)
    return _finish(o, name, mat(m) if isinstance(m, str) else m, True, bevel)

def sphere(name, r=1, loc=(0,0,0), scale=(1,1,1), rot=(0,0,0), m='white', seg=32, rings=16):
    bpy.ops.mesh.primitive_uv_sphere_add(segments=seg, ring_count=rings, radius=r)
    o = bpy.context.active_object
    _xf(o, loc, rot, scale)
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    return _finish(o, name, mat(m) if isinstance(m, str) else m, True)

def torus(name, R, r, loc=(0,0,0), rot=(0,0,0), m='white', seg=32, mseg=12, scale=(1,1,1)):
    bpy.ops.mesh.primitive_torus_add(major_radius=R, minor_radius=r, major_segments=seg, minor_segments=mseg)
    o = bpy.context.active_object
    _xf(o, loc, rot, scale)
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    return _finish(o, name, mat(m) if isinstance(m, str) else m, True)

def capsule(name, r, length, a, b_, m='white', seg=24):
    """Capsule between points a and b (rounded limb)."""
    a, b_ = Vector(a), Vector(b_)
    d = b_ - a; L = d.length
    bpy.ops.mesh.primitive_uv_sphere_add(segments=seg, ring_count=12, radius=r)
    o = bpy.context.active_object
    bm = bmesh.new(); bm.from_mesh(o.data)
    for v in bm.verts:
        if v.co.z > 0: v.co.z += L
    bm.to_mesh(o.data); bm.free()
    o.rotation_euler = d.to_track_quat('Z', 'Y').to_euler()
    o.location = a
    return _finish(o, name, mat(m) if isinstance(m, str) else m, True)

def lathe(name, profile, m='white', seg=48, loc=(0,0,0), rot=(0,0,0)):
    """profile: list of (radius, z) from bottom centre to top centre. Produces a closed, smooth solid."""
    bm = bmesh.new()
    rings = []
    for (r, z) in profile:
        ring = []
        for i in range(seg):
            t = 2 * math.pi * i / seg
            ring.append(bm.verts.new((r * math.cos(t), r * math.sin(t), z)))
        rings.append(ring)
    for k in range(len(rings) - 1):
        for i in range(seg):
            j = (i + 1) % seg
            vs = [rings[k][i], rings[k][j], rings[k + 1][j], rings[k + 1][i]]
            try: bm.faces.new(vs)
            except ValueError: pass
    bmesh.ops.remove_doubles(bm, verts=bm.verts, dist=1e-5)
    bmesh.ops.recalc_face_normals(bm, faces=bm.faces)
    me = bpy.data.meshes.new(name); bm.to_mesh(me); bm.free()
    o = bpy.data.objects.new(name, me); bpy.context.collection.objects.link(o)
    _xf(o, loc, rot)
    return _finish(o, name, mat(m) if isinstance(m, str) else m, True)

def join(name, objs, parent=None):
    bpy.ops.object.select_all(action='DESELECT')
    for o in objs:
        o.select_set(True)
        for mname in [md.name for md in o.modifiers]:
            bpy.context.view_layer.objects.active = o
            bpy.ops.object.modifier_apply(modifier=mname)
    bpy.context.view_layer.objects.active = objs[0]
    bpy.ops.object.join()
    o = bpy.context.active_object
    o.name = name; o.data.name = name
    if parent: o.parent = parent
    return o

def group(name, children, loc=(0,0,0)):
    e = bpy.data.objects.new(name, None); bpy.context.collection.objects.link(e)
    e.location = Vector(loc)
    for c in children: c.parent = e
    return e

def export(path):
    bpy.ops.object.select_all(action='SELECT')
    bpy.ops.export_scene.gltf(filepath=path, export_format='GLB', export_apply=True, export_yup=True,
                              export_texcoords=True, export_normals=True, export_materials='EXPORT',
                              export_cameras=False, export_lights=False, export_animations=False)

def stats():
    tris = 0
    dg = bpy.context.evaluated_depsgraph_get()
    for o in bpy.data.objects:
        if o.type == 'MESH':
            me = o.evaluated_get(dg).to_mesh(); me.calc_loop_triangles(); tris += len(me.loop_triangles)
    return tris
