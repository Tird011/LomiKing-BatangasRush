# Turns Mang Kaloy face drawings into transparent face-decal textures + one emote atlas.
import numpy as np
from PIL import Image
from scipy import ndimage as ndi
import sys
# usage: python make_face_atlas.py <folder with the 7 face drawings, named by their original upload ids>
up = (sys.argv[1] if len(sys.argv) > 1 else '.').rstrip('/') + '/'
ORDER=[('happy_masked','15c48ac7'),('happy','af9168b4'),('blow','a2dd3a42'),('laugh','66119f74'),
       ('shocked','94e01606'),('nervous','6d381e41'),('uh_oh','75f671b5')]
CROP=(470,590,1530,1650)   # px window that maps onto the 3D face (see build.py FACE_MAP)
out='../mobile/assets/textures/'
atlas=Image.new('RGBA',(2048,1024),(0,0,0,0))
for i,(name,f) in enumerate(ORDER):
    a=np.array(Image.open(up+f+'-image.png').convert('RGBA')).astype(int)
    al=a[...,3]>128
    lab,n=ndi.label(al); s=ndi.sum(al,lab,range(1,n+1)); head=ndi.binary_fill_holes(lab==np.argmax(s)+1)
    dark=al&(a[...,:3].sum(2)<200)
    er=ndi.binary_erosion(dark,iterations=14); l2,n2=ndi.label(er)
    s2=ndi.sum(er,l2,range(1,n2+1)); hair=ndi.binary_dilation(l2==np.argmax(s2)+1,iterations=26)
    inner=ndi.binary_erosion(head,iterations=22)
    skin=np.abs(a[...,:3]-[232,168,103]).sum(2)<70
    keep=inner&~hair&~skin
    rgba=a.copy().astype(np.uint8); rgba[...,3]=np.where(keep,255,0)
    im=Image.fromarray(rgba).crop(CROP).resize((512,512),Image.LANCZOS)
    im.save(out+f'face_{name}.png')
    atlas.paste(im,((i%4)*512,(i//4)*512))
atlas.save(out+'face_atlas.png')
print('ok')
