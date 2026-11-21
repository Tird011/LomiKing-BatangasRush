# LomiKing logo: fills the hand-drawn letter outlines and styles them as glossy candy-style lettering.
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
from scipy import ndimage as ndi
import sys
SRC = sys.argv[1] if len(sys.argv) > 1 else 'logo_sketch.png'   # your hand-drawn outline
a=np.array(Image.open(SRC).convert('RGBA')).astype(int)
line=(a[...,3]>100)&(a[...,:3].sum(2)<300)
closed=ndi.binary_dilation(line,iterations=20)
PAD=220
line=np.pad(line,PAD); closed=np.pad(closed,PAD)
lab,n=ndi.label(~closed)
# grow regions back over the (thickened) lines so letters keep their full size
idx=ndi.distance_transform_edt(lab==0,return_distances=False,return_indices=True)
lab=lab[idx[0],idx[1]]
k=1.06
def at(x,y): return lab[int(y*k)+PAD,int(x*k)+PAD]
seeds={'L':(300,700),'swoosh':(950,950),'o':(600,690),'m':(980,500),'i1':(1640,600),'dot1':(1600,105),
       'K':(500,1600),'dot2':(1070,1110),'i2':(1060,1450),'n':(1360,1600),'g':(1830,1500)}
ids={k_:at(*v) for k_,v in seeds.items()}
bg={at(5,5),at(720,685),at(1705,1315),at(1095,600),at(550,775)}
print(ids, bg)
assert not (set(ids.values())&bg)
def region(*names): return np.isin(lab,[ids[x] for x in names])
M=np.isin(lab,list(set(ids.values())))
EGG=region('dot1'); DROP=region('dot2')
def smooth(m,s=4): return ndi.gaussian_filter(m.astype(float),s)>0.5
M,EGG,DROP=smooth(M),smooth(EGG),smooth(DROP)
H,W=M.shape
def dil(m,r): return ndi.distance_transform_edt(~m)<=r

# --- crown on the L's top-left knob (King!) ---
crown=Image.new('L',(W,H),0); cd=ImageDraw.Draw(crown)
cx,cy,s=255+PAD,150+PAD,1.0   # base centre (original px)
pts=[(-150,40),(-165,-120),(-85,-40),(0,-165),(85,-40),(165,-120),(150,40)]
rot=np.radians(-12); R=np.array([[np.cos(rot),-np.sin(rot)],[np.sin(rot),np.cos(rot)]])
P=[tuple(np.array([cx,cy])+R@np.array(p)) for p in pts]
cd.polygon(P,fill=255); C=np.array(crown)>0
gems=[tuple(np.array([cx,cy])+R@np.array(p)) for p in [(-165,-120),(0,-165),(165,-120)]]

ALL=M|C
out=np.zeros((H,W,4))
def paint(mask,rgb,alpha=1.0):
    m=mask.astype(float)*alpha
    out[...,:3]=out[...,:3]*(1-m[...,None])+np.array(rgb)[None,None,:]/255*m[...,None]
    out[...,3]=np.maximum(out[...,3],m)
# shadow
sh=ndi.gaussian_filter(np.roll(dil(ALL,40),28,axis=0).astype(float),10)
paint(sh>0.02,(90,40,15),0); out[...,:3]=np.where(sh[...,None]>0.02,np.array([90,40,15])/255,out[...,:3]); out[...,3]=np.maximum(out[...,3],sh*0.45)
def bevel(mask,top,bottom,gloss=True):
    D=ndi.distance_transform_edt(mask); h=np.minimum(D,34.0); h=ndi.gaussian_filter(h,3)
    gy,gx=np.gradient(h); nz=np.ones_like(h)*2.2
    nl=np.sqrt(gx**2+gy**2+nz**2); nx,ny,nzz=-gx/nl,-gy/nl,nz/nl
    L=np.array([-0.4,-0.7,0.6]); L/=np.linalg.norm(L)
    d=nx*L[0]+ny*L[1]+nzz*L[2]
    ys=np.where(mask.any(1))[0]; t=np.clip((np.arange(H)-ys.min())/(ys.max()-ys.min()+1),0,1)[:,None]
    base=np.array(top)[None,None,:]*(1-t[...,None])+np.array(bottom)[None,None,:]*t[...,None]
    col=base/255*(0.72+0.42*d[...,None])
    if gloss:
        g=(d>0.93)&(D>6); col=np.where(g[...,None],col*0.35+0.65,col)
    col=np.clip(col,0,1); m=mask
    out[m,:3]=col[m]; out[m,3]=1

def letter(mask,top=(255,246,140),bottom=(250,150,20)):
    paint(dil(mask,36),(74,36,14)); paint(dil(mask,18),(232,112,20)); paint(dil(mask,12),(120,54,16))
    bevel(mask,top,bottom)
for name in ['K','L','o','m','i1','i2','n','g']:
    letter(smooth(region(name)))
letter(EGG,(255,255,255),(236,232,220))
letter(DROP,(230,150,70),(170,80,30))
letter(C,(255,240,120),(240,160,20))
# egg yolk on the i dot
ys,xs=np.where(EGG); yc,xc=ys.mean(),xs.mean()
yy,xx=np.mgrid[0:H,0:W]; yolk=(((xx-xc-8)/62)**2+((yy-yc+4)/44)**2<1)&EGG
paint(dil(yolk,6)&EGG,(220,120,20)); bevel(yolk,(255,214,60),(250,150,20))
# crown gems
for gx_,gy_ in gems:
    g=((xx-gx_)**2+(yy-gy_)**2)<26**2; paint(dil(g,8),(74,36,14)); bevel(g,(255,120,130),(200,30,60))
# steam wisps above the m (lomi is served hot!)
st=Image.new('L',(W,H),0); sd=ImageDraw.Draw(st)
for x0 in (1080,1220,1350):
    pts=[(x0+PAD+28*np.sin(t/22),250+PAD-t) for t in np.arange(0,190,4)]
    sd.line(pts,fill=255,width=26,joint='curve')
S=np.array(st.filter(ImageFilter.GaussianBlur(3)))>128
S&=~dil(ALL,40)
paint(dil(S,10),(74,36,14)); paint(S,(255,255,255))
img=Image.fromarray((out*255).astype(np.uint8),'RGBA')
bbox=img.getbbox(); img=img.crop((bbox[0]-20,bbox[1]-20,bbox[2]+20,bbox[3]+20))
img.save('lomiking_logo.png')
# preview on the game's pink stripes
bgw,bgh=img.size[0]+200,img.size[1]+200
bgi=Image.new('RGBA',(bgw,bgh))
arr=np.zeros((bgh,bgw,4),np.uint8); xs_=np.arange(bgw); st_=((xs_//70)%2)[None,:]
arr[...,0]=255; arr[...,1]=np.where(st_,196,214); arr[...,2]=np.where(st_,200,216); arr[...,3]=255
bgi=Image.fromarray(arr,'RGBA'); bgi.alpha_composite(img,(100,100)); bgi.convert('RGB').resize((bgw//2,bgh//2),Image.LANCZOS).save('lomiking_logo_preview.png')
print(img.size)
