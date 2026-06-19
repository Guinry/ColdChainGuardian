from __future__ import annotations

import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageChops

OUT = Path(r"C:\Users\23869\Desktop\ColdChainGuardian\output\doc\qa_v16\new_diagrams")
OUT.mkdir(parents=True, exist_ok=True)

FONT_REG = r"C:\Windows\Fonts\msyh.ttc"
FONT_BOLD = r"C:\Windows\Fonts\msyhbd.ttc"
if not Path(FONT_BOLD).exists():
    FONT_BOLD = FONT_REG

def font(size:int, bold:bool=False):
    return ImageFont.truetype(FONT_BOLD if bold else FONT_REG, size=size)

def wh(d, text, ft):
    box=d.textbbox((0,0), text, font=ft)
    return box[2]-box[0], box[3]-box[1]

def split_label(text, max_chars=8):
    if '\n' in text:
        return text.split('\n')
    # split on spaces for English/mixed, otherwise by char count
    lines=[]
    cur=''
    for ch in text:
        cur += ch
        if len(cur) >= max_chars:
            lines.append(cur)
            cur=''
    if cur:
        lines.append(cur)
    return lines

def fit_font(d, lines, max_w, max_h, start=45, min_size=28, bold=False):
    for size in range(start, min_size-1, -2):
        ft=font(size,bold)
        gap=max(5,size//6)
        widths=[wh(d,line,ft)[0] for line in lines]
        heights=[wh(d,line,ft)[1] for line in lines]
        total=sum(heights)+gap*(len(lines)-1)
        if (not widths or max(widths)<=max_w) and total<=max_h:
            return ft,gap,heights
    ft=font(min_size,bold)
    return ft,max(5,min_size//6),[wh(d,line,ft)[1] for line in lines]

def center_text(d, box, text, size=42, bold=False, max_chars=8):
    x1,y1,x2,y2=box
    lines=split_label(text,max_chars)
    ft,gap,heights=fit_font(d,lines,x2-x1-28,y2-y1-20,size,28,bold)
    total=sum(heights)+gap*(len(lines)-1)
    y=y1+(y2-y1-total)/2
    for line,h in zip(lines,heights):
        w,_=wh(d,line,ft)
        d.text((x1+(x2-x1-w)/2,y),line,font=ft,fill='black')
        y += h + gap

def rect(d, box, text, size=42, bold=False, max_chars=8, width=4, radius=8):
    d.rounded_rectangle(box, radius=radius, outline='black', width=width, fill='white')
    center_text(d, box, text, size=size, bold=bold, max_chars=max_chars)

def plain_rect(d, box, width=4):
    d.rectangle(box, outline='black', width=width, fill='white')

def line(d, pts, width=4):
    d.line(pts, fill='black', width=width, joint='curve')

def arrow_head(d, p1, p2, size=22):
    ang=math.atan2(p2[1]-p1[1], p2[0]-p1[0])
    left=(p2[0]-size*math.cos(ang-math.pi/6), p2[1]-size*math.sin(ang-math.pi/6))
    right=(p2[0]-size*math.cos(ang+math.pi/6), p2[1]-size*math.sin(ang+math.pi/6))
    d.polygon([p2,left,right],fill='black')

def arrow(d, pts, width=4, head=22):
    line(d, pts, width)
    arrow_head(d, pts[-2], pts[-1], head)

def crop_save(img, name, margin=45):
    bg=Image.new('RGB', img.size, 'white')
    bbox=ImageChops.difference(img,bg).getbbox()
    if bbox:
        x1,y1,x2,y2=bbox
        img=img.crop((max(0,x1-margin),max(0,y1-margin),min(img.width,x2+margin),min(img.height,y2+margin)))
    img.save(OUT/name, dpi=(300,300), quality=95)
    print(OUT/name)

def canvas(w,h):
    img=Image.new('RGB',(w,h),'white')
    return img, ImageDraw.Draw(img)

# Figure 1-1: research background context diagram (no title)
def fig_1_1():
    img,d=canvas(2300,1120)
    # central warehouse context
    rect(d,(860,430,1440,620),'冷链仓储\n安全管理对象',48,True,10,width=5)
    left=[(100,120,'温湿度\n传感器'),(100,330,'制冷设备'),(100,540,'库区货位'),(100,750,'现场人员')]
    right=[(1770,120,'实时监测'),(1770,330,'阈值告警'),(1770,540,'工单闭环'),(1770,750,'智能分析')]
    for x,y,t in left:
        rect(d,(x,y,x+360,y+120),t,42,False,8)
        arrow(d,[(x+360,y+60),(760,y+60),(760,490),(860,490)],4)
    for x,y,t in right:
        rect(d,(x,y,x+360,y+120),t,42,False,8)
        arrow(d,[(1440,550),(1600,550),(1600,y+60),(x,y+60)],4)
    rect(d,(650,820,1030,960),'数据库\n历史记录',42,False,8)
    rect(d,(1270,820,1650,960),'大语言模型\n辅助研判',42,False,8)
    arrow(d,[(1140,620),(840,820)],4)
    arrow(d,[(1160,620),(1460,820)],4)
    arrow(d,[(1030,890),(1270,890)],4)
    crop_save(img,'fig_1_1_cold_chain_context.png')

# Figure 1-2: research contents structure
def fig_1_2():
    img,d=canvas(1700,1420)
    rect(d,(540,60,1160,160),'研究内容组织',58,True,10,width=5)
    modules=[
        (240,'业务需求分析','角色、功能、流程、用例'),
        (455,'系统总体设计','架构、模块、数据库、接口'),
        (670,'详细设计实现','Web 管理端、小程序端、后端服务'),
        (885,'智能助手设计','上下文检索、模型调用、结构化输出'),
        (1100,'系统测试验证','功能、接口、页面、异常场景')]
    prev_bottom=160
    for y,title,sub in modules:
        rect(d,(190,y,650,y+110),title,48,True,8)
        rect(d,(870,y,1510,y+110),sub,42,False,14)
        arrow(d,[(650,y+55),(870,y+55)],5,24)
        arrow(d,[(850,prev_bottom),(850,y)],5,24)
        line(d,[(850,y+55),(870,y+55)],5)
        prev_bottom=y+110
    arrow(d,[(850,1210),(850,1300)],5,24)
    rect(d,(395,1300,1305,1390),'围绕冷链仓储异常发现、处置、分析与复盘展开',40,False,18,width=4)
    crop_save(img,'fig_1_2_research_contents.png')

# Figure 2-1: key technology relationship
def fig_2_1():
    img,d=canvas(1700,1580)
    layers=[
        (80,90,1620,320,'表现层','Vue 3 / Element Plus\nECharts','微信小程序'),
        (80,410,1620,640,'接入与通信层','RESTful API\nJWT 认证','WebSocket 实时推送'),
        (80,730,1620,960,'业务服务层','Spring Boot\n分层服务','阈值规则 / 告警工单\nAI 助手'),
        (80,1050,1620,1280,'数据与外部层','MySQL / Redis\nMyBatis-Plus','兼容式大模型接口')]
    for x1,y1,x2,y2,name,a,b in layers:
        plain_rect(d,(x1,y1,x2,y2),5)
        rect(d,(x1+35,y1+55,x1+355,y2-55),name,48,True,7,width=4)
        rect(d,(x1+465,y1+52,x1+945,y2-52),a,43,False,15,width=4)
        rect(d,(x1+1050,y1+52,x2-45,y2-52),b,43,False,15,width=4)
    for y in [320,640,960]:
        arrow(d,[(850,y),(850,y+90)],6,28)
    rect(d,(120,1390,520,1530),'身份校验\n状态同步',42,False,8)
    rect(d,(650,1390,1050,1530),'数据持久化\n缓存加速',42,False,8)
    rect(d,(1180,1390,1580,1530),'智能问答\n异常兜底',42,False,8)
    arrow(d,[(320,1390),(500,1280)],5,22)
    arrow(d,[(850,1390),(850,1280)],5,22)
    arrow(d,[(1380,1390),(1200,1280)],5,22)
    crop_save(img,'fig_2_1_key_technology_relation.png')

# Figure 3-5: requirements to design mapping
def fig_3_5():
    img,d=canvas(1700,1420)
    reqs=[('角色需求',150),('功能需求',330),('非功能需求',510),('业务流程',690)]
    mods=[('认证权限模块',150),('监测告警模块',330),('工单协同模块',510),('数据分析模块',690),('AI 助手模块',870)]
    for t,y in reqs:
        rect(d,(90,y,430,y+105),t,46,True,8)
    for t,y in mods:
        rect(d,(1270,y,1620,y+105),t,45,True,8)
    rect(d,(600,120,1100,265),'需求分析阶段\n明确系统边界与业务场景',44,False,13,width=5)
    rect(d,(600,470,1100,615),'概要设计阶段\n划分模块职责与数据关系',44,False,13,width=5)
    rect(d,(600,820,1100,965),'详细设计阶段\n落实页面、接口与流程',44,False,13,width=5)
    for _t,y in reqs:
        arrow(d,[(430,y+52),(600,192)],5,20)
    arrow(d,[(850,265),(850,470)],6,26)
    arrow(d,[(850,615),(850,820)],6,26)
    branch = (1135, 542)
    line(d, [(1100, 542), branch], 5)
    for idx, (_t, y) in enumerate(mods):
        stub_x = 1210 if idx in (0, 4) else 1185
        arrow(d, [branch, (stub_x, y + 52), (1270, y + 52)], 5, 20)
    rect(d,(280,1135,1420,1280),'用例图提供需求依据，模块结构图完成设计细化，\n两者形成多对多映射关系',42,False,24,width=4)
    arrow(d,[(850,965),(850,1135)],5,22)
    crop_save(img,'fig_3_5_requirement_design_mapping.png')

fig_1_1(); fig_1_2(); fig_2_1(); fig_3_5()
