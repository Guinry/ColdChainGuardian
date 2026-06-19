from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


OUT = Path(r"C:\Users\23869\Desktop\ColdChainGuardian\output\doc\qa_v11\diagrams")
OUT.mkdir(parents=True, exist_ok=True)


def font_path() -> str:
    candidates = [
        r"C:\Windows\Fonts\msyh.ttc",
        r"C:\Windows\Fonts\simhei.ttf",
        r"C:\Windows\Fonts\simsun.ttc",
        r"C:\Windows\Fonts\arial.ttf",
    ]
    for path in candidates:
        if Path(path).exists():
            return path
    raise FileNotFoundError("No usable font found")


FONT = font_path()


def f(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    # Microsoft YaHei contains readable Chinese glyphs. The TTC regular face is
    # sufficient at the sizes used here; line precision matters more than weight.
    return ImageFont.truetype(FONT, size=size)


def canvas(w: int, h: int) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    img = Image.new("RGB", (w, h), "white")
    return img, ImageDraw.Draw(img)


def text_size(d: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont) -> tuple[int, int]:
    box = d.textbbox((0, 0), text, font=font)
    return box[2] - box[0], box[3] - box[1]


def center_text(
    d: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    text: str,
    font: ImageFont.FreeTypeFont,
    fill: str = "black",
    line_gap: int = 8,
) -> None:
    x1, y1, x2, y2 = box
    lines = text.split("\n")
    heights = [text_size(d, line, font)[1] for line in lines]
    total_h = sum(heights) + line_gap * (len(lines) - 1)
    y = y1 + (y2 - y1 - total_h) / 2
    for line, height in zip(lines, heights):
        w, _ = text_size(d, line, font)
        d.text((x1 + (x2 - x1 - w) / 2, y), line, font=font, fill=fill)
        y += height + line_gap


def rect(d, xy, label, font, width=4, radius=16, dash=False):
    if dash:
        dashed_rect(d, xy, width=width, dash=18)
    elif radius:
        d.rounded_rectangle(xy, radius=radius, outline="black", width=width, fill="white")
    else:
        d.rectangle(xy, outline="black", width=width, fill="white")
    if label:
        center_text(d, xy, label, font)


def ellipse(d, xy, label, font, width=4):
    d.ellipse(xy, outline="black", width=width, fill="white")
    center_text(d, xy, label, font)


def line(d, p1, p2, width=4):
    d.line([p1, p2], fill="black", width=width)


def arrow(d, p1, p2, width=4, head=20):
    d.line([p1, p2], fill="black", width=width)
    angle = math.atan2(p2[1] - p1[1], p2[0] - p1[0])
    left = (p2[0] - head * math.cos(angle - math.pi / 6), p2[1] - head * math.sin(angle - math.pi / 6))
    right = (p2[0] - head * math.cos(angle + math.pi / 6), p2[1] - head * math.sin(angle + math.pi / 6))
    d.polygon([p2, left, right], fill="black")


def poly_arrow(d, points, width=4, head=20):
    for a, b in zip(points, points[1:]):
        d.line([a, b], fill="black", width=width)
    arrow(d, points[-2], points[-1], width=width, head=head)


def double_arrow(d, p1, p2, width=4, head=18):
    arrow(d, p1, p2, width=width, head=head)
    arrow(d, p2, p1, width=width, head=head)


def dashed_rect(d, xy, width=4, dash=16):
    x1, y1, x2, y2 = xy
    dashed_line(d, (x1, y1), (x2, y1), width, dash)
    dashed_line(d, (x2, y1), (x2, y2), width, dash)
    dashed_line(d, (x2, y2), (x1, y2), width, dash)
    dashed_line(d, (x1, y2), (x1, y1), width, dash)


def dashed_line(d, p1, p2, width=4, dash=16):
    x1, y1 = p1
    x2, y2 = p2
    length = math.hypot(x2 - x1, y2 - y1)
    if length == 0:
        return
    dx = (x2 - x1) / length
    dy = (y2 - y1) / length
    pos = 0
    while pos < length:
        end = min(pos + dash, length)
        if int(pos / dash) % 2 == 0:
            d.line([(x1 + dx * pos, y1 + dy * pos), (x1 + dx * end, y1 + dy * end)], fill="black", width=width)
        pos += dash


def actor(d, cx, cy, label, font):
    d.ellipse((cx - 28, cy - 90, cx + 28, cy - 34), outline="black", width=4)
    line(d, (cx, cy - 34), (cx, cy + 50), 4)
    line(d, (cx - 65, cy), (cx + 65, cy), 4)
    line(d, (cx, cy + 50), (cx - 60, cy + 120), 4)
    line(d, (cx, cy + 50), (cx + 60, cy + 120), 4)
    center_text(d, (cx - 160, cy + 130, cx + 160, cy + 210), label, font)


def save(img: Image.Image, name: str) -> None:
    img.save(OUT / name, quality=95)


def diagram_roles():
    img, d = canvas(2200, 1128)
    title_font, body = f(58), f(50)
    roles = [
        ((120, 170, 520, 300), "系统管理员"),
        ((120, 410, 520, 540), "仓储管理员"),
        ((120, 650, 520, 780), "现场员工"),
        ((120, 890, 520, 1020), "大模型服务"),
    ]
    modules = [
        ((810, 120, 1220, 230), "认证权限"),
        ((810, 280, 1220, 390), "库区设备"),
        ((810, 440, 1220, 550), "监测预警"),
        ((810, 600, 1220, 710), "告警工单"),
        ((810, 760, 1220, 870), "数据分析"),
        ((810, 920, 1220, 1030), "智能问答"),
    ]
    permissions = [
        ((1540, 180, 1980, 310), "用户与角色管理"),
        ((1540, 380, 1980, 510), "业务数据维护"),
        ((1540, 580, 1980, 710), "现场处置反馈"),
        ((1540, 780, 1980, 910), "辅助分析建议"),
    ]
    for box, label in roles:
        rect(d, box, label, body)
    for box, label in modules:
        rect(d, box, label, body)
    for box, label in permissions:
        rect(d, box, label, body)
    for rb, _ in roles:
        for mb, _ in modules:
            if rb[1] < 360 and mb[1] in (120, 760, 920):
                arrow(d, (rb[2], (rb[1]+rb[3])//2), (mb[0], (mb[1]+mb[3])//2), 3, 16)
            elif 360 < rb[1] < 600 and mb[1] in (120, 280, 440, 600, 760, 920):
                arrow(d, (rb[2], (rb[1]+rb[3])//2), (mb[0], (mb[1]+mb[3])//2), 3, 16)
            elif rb[1] > 600 and rb[1] < 850 and mb[1] in (120, 600):
                arrow(d, (rb[2], (rb[1]+rb[3])//2), (mb[0], (mb[1]+mb[3])//2), 3, 16)
            elif rb[1] > 850 and mb[1] == 920:
                arrow(d, (rb[2], (rb[1]+rb[3])//2), (mb[0], (mb[1]+mb[3])//2), 3, 16)
    for mb, _ in modules:
        target = permissions[0][0] if mb[1] == 120 else permissions[1][0] if mb[1] in (280, 440) else permissions[2][0] if mb[1] == 600 else permissions[3][0]
        arrow(d, (mb[2], (mb[1]+mb[3])//2), (target[0], (target[1]+target[3])//2), 3, 16)
    save(img, "image1_roles_permissions.png")


def diagram_business_flow():
    img, d = canvas(2200, 1250)
    body, small = f(54), f(46)
    y = 170
    boxes = [
        (120, y, 420, y + 120, "设备采集"),
        (540, y, 840, y + 120, "数据入库"),
        (960, y, 1260, y + 120, "阈值判断"),
        (1380, y, 1680, y + 120, "生成告警"),
        (1800, y, 2100, y + 120, "实时推送"),
    ]
    for b in boxes:
        rect(d, b[:4], b[4], body)
    for a, b in zip(boxes, boxes[1:]):
        arrow(d, (a[2], y + 60), (b[0], y + 60), 4)
    y2 = 500
    boxes2 = [
        (330, y2, 650, y2 + 120, "告警研判"),
        (830, y2, 1150, y2 + 120, "转为工单"),
        (1330, y2, 1650, y2 + 120, "现场处理"),
        (1830, y2, 2150, y2 + 120, "验收关闭"),
    ]
    for b in boxes2:
        rect(d, b[:4], b[4], body)
    poly_arrow(d, [(1950, 290), (1950, 400), (490, 400), (490, 500)])
    for a, b in zip(boxes2, boxes2[1:]):
        arrow(d, (a[2], y2 + 60), (b[0], y2 + 60), 4)
    stats = [
        (330, 840, 650, 960, "闭环记录"),
        (830, 840, 1150, 960, "趋势统计"),
        (1330, 840, 1650, 960, "风险分析"),
        (1830, 840, 2150, 960, "复盘优化"),
    ]
    for b in stats:
        rect(d, b[:4], b[4], body)
    for i, b in enumerate(stats):
        arrow(d, ((boxes2[i][0]+boxes2[i][2])//2, y2+120), ((b[0]+b[2])//2, b[1]), 4)
    for a, b in zip(stats, stats[1:]):
        arrow(d, (a[2], 900), (b[0], 900), 4)
    center_text(d, (120, 1040, 2080, 1140), "数据采集、告警研判、工单处置和统计分析共同形成冷链仓储安全管理闭环", small)
    save(img, "image2_business_flow.png")


def diagram_alert_loop():
    img, d = canvas(2200, 1125)
    body = f(54)
    nodes = {
        "start": (120, 470, 360, 590, "告警生成"),
        "review": (520, 470, 760, 590, "管理研判"),
        "ignore": (520, 760, 760, 880, "误报关闭"),
        "solve": (920, 760, 1160, 880, "直接解决"),
        "order": (920, 470, 1160, 590, "转为工单"),
        "accept": (1320, 470, 1560, 590, "员工接收"),
        "handle": (1720, 470, 1960, 590, "现场处理"),
        "check": (1720, 760, 1960, 880, "管理验收"),
        "close": (1320, 760, 1560, 880, "闭环归档"),
    }
    for xy in nodes.values():
        rect(d, xy[:4], xy[4], body)
    arrow(d, (360, 530), (520, 530), 4)
    arrow(d, (760, 530), (920, 530), 4)
    arrow(d, (1160, 530), (1320, 530), 4)
    arrow(d, (1560, 530), (1720, 530), 4)
    arrow(d, (1840, 590), (1840, 760), 4)
    arrow(d, (1720, 820), (1560, 820), 4)
    arrow(d, (1320, 820), (1160, 820), 4)
    arrow(d, (760, 590), (760, 760), 4)
    arrow(d, (880, 530), (1040, 760), 4)
    poly_arrow(d, [(1840, 760), (1840, 1010), (1040, 1010), (1040, 880)], 4)
    center_text(d, (1780, 920, 2080, 1010), "不通过退回", f(42))
    save(img, "image3_alert_loop.png")


def diagram_use_case():
    img, d = canvas(2200, 1000)
    body, small = f(58), f(48)
    boundary = (440, 80, 1760, 880)
    rect(d, boundary, "", body, width=4, radius=0)
    center_text(d, (480, 100, 850, 170), "系统边界", small)
    actors = {
        "系统管理员": (220, 210),
        "仓储管理员": (220, 650),
        "现场员工": (1980, 250),
        "大模型服务": (1980, 670),
    }
    for label, (x, y) in actors.items():
        actor(d, x, y, label, body)
    use_cases = {
        "登录认证": (720, 150, 1080, 250),
        "系统管理": (720, 320, 1080, 420),
        "库区管理": (720, 490, 1080, 590),
        "设备管理": (720, 660, 1080, 760),
        "实时监测": (1190, 150, 1550, 250),
        "阈值规则": (1190, 320, 1550, 420),
        "告警处理": (1190, 490, 1550, 590),
        "工单闭环": (1190, 660, 1550, 760),
        "数据分析": (720, 800, 1080, 900),
        "AI 智能问答": (1190, 800, 1550, 900),
        "小程序工单处理": (1190, 930, 1550, 1030),
    }
    for label, xy in use_cases.items():
        ellipse(d, xy, label, body)

    def left_edge(xy): return (xy[0], (xy[1] + xy[3]) // 2)
    def right_edge(xy): return (xy[2], (xy[1] + xy[3]) // 2)

    # Use orthogonal association lines with small buses. This keeps every
    # segment straight and avoids associations running through use-case labels.
    def assoc_left(start, keys, bus_x):
        line(d, start, (bus_x, start[1]), 3)
        ys = [(use_cases[k][1] + use_cases[k][3]) // 2 for k in keys]
        line(d, (bus_x, min(ys)), (bus_x, max(ys)), 3)
        for key in keys:
            y = (use_cases[key][1] + use_cases[key][3]) // 2
            line(d, (bus_x, y), left_edge(use_cases[key]), 3)

    def assoc_right(start, keys, bus_x):
        line(d, start, (bus_x, start[1]), 3)
        ys = [(use_cases[k][1] + use_cases[k][3]) // 2 for k in keys]
        line(d, (bus_x, min(ys)), (bus_x, max(ys)), 3)
        for key in keys:
            y = (use_cases[key][1] + use_cases[key][3]) // 2
            line(d, (bus_x, y), right_edge(use_cases[key]), 3)

    assoc_left((360, 210), ["登录认证", "系统管理", "数据分析"], 600)
    assoc_left((360, 650), ["库区管理", "设备管理", "实时监测", "阈值规则", "告警处理", "工单闭环"], 640)
    assoc_right((1840, 250), ["登录认证", "告警处理", "工单闭环"], 1650)
    assoc_right((1840, 670), ["AI 智能问答"], 1650)
    save(img, "image4_use_case.png")


def diagram_architecture():
    img, d = canvas(2200, 1340)
    layer_font, body, small = f(54), f(46), f(42)
    layers = [
        (70, 80, 2130, 290, "表现层", ["Web 管理端", "微信小程序端", "PC 浏览器"]),
        (70, 390, 2130, 600, "接入层", ["REST API", "JWT 认证", "WebSocket 推送"]),
        (70, 700, 2130, 910, "业务层", ["库区管理", "设备管理", "实时监测", "阈值规则", "告警中心", "工单闭环", "数据分析", "AI 助手"]),
        (70, 1010, 2130, 1240, "数据与\n外部服务层", ["MySQL", "Redis", "日志审计", "大模型接口"]),
    ]
    for x1, y1, x2, y2, label, items in layers:
        rect(d, (x1, y1, x2, y2), "", body, radius=12)
        center_text(d, (x1 + 10, y1, x1 + 260, y2), label, layer_font)
        n = len(items)
        left = x1 + 320
        gap = 28
        bw = (x2 - left - 60 - gap * (n - 1)) // n
        for i, item in enumerate(items):
            bx1 = left + i * (bw + gap)
            rect(d, (bx1, y1 + 55, bx1 + bw, y2 - 55), item, small if n > 4 else body)
    for x in [570, 1100, 1630]:
        arrow(d, (x, 290), (x, 390), 4)
        arrow(d, (x, 600), (x, 700), 4)
    double_arrow(d, (600, 910), (600, 1010), 4)
    double_arrow(d, (1100, 910), (1100, 1010), 4)
    arrow(d, (1480, 910), (1480, 1010), 4)
    double_arrow(d, (1950, 910), (1950, 1010), 4)
    save(img, "image5_architecture.png")


def diagram_deployment():
    img, d = canvas(2200, 980)
    head, body, small = f(54), f(48), f(40)
    cols = [
        (40, 70, 390, 900, "客户端"),
        (520, 70, 1130, 900, "应用服务"),
        (1260, 70, 1720, 900, "数据服务"),
        (1820, 70, 2160, 900, "外部服务"),
    ]
    for x1, y1, x2, y2, label in cols:
        dashed_rect(d, (x1, y1, x2, y2), 4, 22)
        center_text(d, (x1, y1 + 20, x2, y1 + 100), label, head)
    rect(d, (90, 220, 340, 380), "Web 管理端", body)
    rect(d, (90, 560, 340, 720), "微信小程序端", body)
    rect(d, (610, 190, 1040, 390), "Spring Boot\n后端服务\n端口 8080", body)
    rect(d, (610, 520, 1040, 680), "WebSocket 服务", body)
    rect(d, (610, 760, 1040, 880), "AI 上下文构造", body)
    rect(d, (1320, 180, 1660, 330), "MySQL 数据库", body)
    rect(d, (1320, 455, 1660, 605), "Redis 缓存", body)
    rect(d, (1320, 720, 1660, 870), "文件与日志", body)
    rect(d, (1845, 710, 2135, 880), "兼容式大模型接口\ndeepseek-v4-pro", small)
    arrow(d, (340, 300), (610, 300), 4)
    arrow(d, (340, 640), (610, 600), 4)
    center_text(d, (390, 210, 560, 280), "HTTPS/\nREST API", small)
    center_text(d, (390, 535, 560, 605), "HTTPS/\nREST API", small)
    double_arrow(d, (1040, 270), (1320, 255), 4)
    double_arrow(d, (1040, 345), (1320, 530), 4)
    double_arrow(d, (825, 390), (825, 520), 4)
    poly_arrow(d, [(1040, 820), (1185, 820), (1185, 255), (1320, 255)], 4)
    arrow(d, (1040, 820), (1845, 795), 4)
    save(img, "image6_deployment.png")


def diagram_modules():
    img, d = canvas(2400, 980)
    body, small = f(54), f(44)
    root = (760, 40, 1640, 150)
    rect(d, root, "冷链仓储安全管理系统", body)
    groups = [
        ((100, 230, 2300, 410), "Web 管理端", ["Dashboard", "库区管理", "设备管理", "实时监测", "阈值规则", "告警中心", "工单中心", "数据分析", "系统管理", "AI 智能助手"], 5),
        ((100, 510, 2300, 670), "微信小程序端", ["登录", "工作台", "告警查看", "工单处理", "个人中心"], 5),
        ((100, 760, 2300, 920), "后端服务", ["认证授权", "业务接口", "实时推送", "数据持久化", "模型接口适配"], 5),
    ]
    for box, label, items, cols in groups:
        rect(d, box, "", body, radius=10)
        rect(d, (box[0] + 30, box[1] + 45, box[0] + 300, box[3] - 45), label, body, width=3)
        arrow(d, ((root[0] + root[2]) // 2, root[3]), ((box[0] + box[2]) // 2, box[1]), 4)
        x0 = box[0] + 350
        y0 = box[1] + 30
        bw = 330
        bh = 60
        gapx = 35
        gapy = 35
        for i, item in enumerate(items):
            row = i // cols
            col = i % cols
            bx = (x0 + col * (bw + gapx), y0 + row * (bh + gapy), x0 + col * (bw + gapx) + bw, y0 + row * (bh + gapy) + bh)
            rect(d, bx, item, small, width=3, radius=10)
    save(img, "image7_modules.png")


def diagram_er():
    img, d = canvas(2200, 1300)
    body, small = f(44), f(34)
    entities = {
        "用户": (120, 90, 470, 260, "用户\nuser_id, username\nrole_id, status"),
        "角色": (650, 90, 1000, 260, "角色\nrole_id, role_name\npermission_set"),
        "库区": (120, 410, 470, 580, "库区\narea_id, parent_id\nname, path"),
        "设备": (650, 410, 1000, 580, "设备\ndevice_id, area_id\ntype, status"),
        "监测数据": (1180, 410, 1530, 580, "监测数据\ndata_id, device_id\ntemp, humidity"),
        "阈值规则": (650, 730, 1000, 900, "阈值规则\nrule_id, target_id\nmin, max"),
        "告警": (1180, 730, 1530, 900, "告警\nalert_id, device_id\nlevel, status"),
        "工单": (1710, 730, 2060, 900, "工单\norder_id, alert_id\nassignee, state"),
        "AI 会话": (1180, 1030, 1530, 1200, "AI 会话\nsession_id, user_id\ncreated_at"),
        "AI 消息": (1710, 1030, 2060, 1200, "AI 消息\nmessage_id, session_id\nrole, content"),
    }
    for xy in entities.values():
        rect(d, xy[:4], xy[4], small, radius=8)
    double_arrow(d, (470, 175), (650, 175), 4)
    center_text(d, (515, 130, 605, 165), "N:1", small)
    double_arrow(d, (470, 495), (650, 495), 4)
    center_text(d, (515, 450, 605, 485), "1:N", small)
    double_arrow(d, (1000, 495), (1180, 495), 4)
    center_text(d, (1045, 450, 1135, 485), "1:N", small)
    double_arrow(d, (825, 580), (825, 730), 4)
    center_text(d, (850, 640, 960, 680), "匹配", small)
    double_arrow(d, (1355, 580), (1355, 730), 4)
    center_text(d, (1380, 640, 1490, 680), "触发", small)
    double_arrow(d, (1530, 815), (1710, 815), 4)
    center_text(d, (1575, 770, 1665, 805), "转化", small)
    double_arrow(d, (825, 260), (1355, 1030), 4)
    double_arrow(d, (1530, 1115), (1710, 1115), 4)
    save(img, "image8_er.png")


def uml_box(d, xy, name, attrs, methods, fonts):
    x1, y1, x2, y2 = xy
    d.rectangle(xy, outline="black", width=4, fill="white")
    h1 = 58
    h2 = 62 + len(attrs) * 38
    d.line((x1, y1 + h1, x2, y1 + h1), fill="black", width=3)
    d.line((x1, y1 + h2, x2, y1 + h2), fill="black", width=3)
    center_text(d, (x1, y1, x2, y1 + h1), name, fonts[0])
    y = y1 + h1 + 12
    for a in attrs:
        d.text((x1 + 18, y), a, font=fonts[1], fill="black")
        y += 38
    y = y1 + h2 + 12
    for m in methods:
        d.text((x1 + 18, y), m, font=fonts[1], fill="black")
        y += 38


def diagram_class():
    img, d = canvas(2200, 1375)
    head, small = f(38), f(30)
    classes = {
        "User": ((80, 80, 420, 330), ["id", "username", "roleId"], ["login()", "hasRole()"]),
        "Area": ((610, 80, 950, 330), ["id", "parentId", "name"], ["buildTree()", "update()"]),
        "Device": ((1140, 80, 1480, 330), ["id", "areaId", "status"], ["enable()", "bindRule()"]),
        "SensorData": ((1670, 80, 2050, 330), ["id", "deviceId", "temp"], ["save()", "latest()"]),
        "Alert": ((350, 560, 720, 810), ["id", "deviceId", "level"], ["confirm()", "resolve()"]),
        "WorkOrder": ((920, 560, 1290, 810), ["id", "alertId", "state"], ["assign()", "accept()"]),
        "AiSession": ((1490, 560, 1860, 810), ["id", "userId", "title"], ["create()", "summarize()"]),
        "AiMessage": ((1490, 1000, 1860, 1250), ["id", "sessionId", "role"], ["append()", "render()"]),
    }
    for name, (xy, attrs, methods) in classes.items():
        uml_box(d, xy, name, attrs, methods, (head, small))
    double_arrow(d, (420, 205), (610, 205), 4)
    double_arrow(d, (950, 205), (1140, 205), 4)
    double_arrow(d, (1480, 205), (1670, 205), 4)
    double_arrow(d, (1310, 330), (650, 560), 4)
    double_arrow(d, (720, 685), (920, 685), 4)
    double_arrow(d, (1675, 810), (1675, 1000), 4)
    double_arrow(d, (420, 330), (1540, 560), 4)
    save(img, "image9_class.png")


def diagram_sequence_login():
    img, d = canvas(2200, 1215)
    body, small = f(44), f(34)
    xs = [250, 650, 1050, 1450, 1850]
    labels = ["用户", "Web 登录页", "AuthController", "UserService", "数据库"]
    for x, label in zip(xs, labels):
        rect(d, (x - 145, 80, x + 145, 170), label, body)
        dashed_line(d, (x, 170), (x, 1110), 3, 18)
    msgs = [
        (250, 650, 260, "输入账号密码"),
        (650, 1050, 380, "POST /auth/login"),
        (1050, 1450, 500, "校验登录参数"),
        (1450, 1850, 620, "查询用户与角色"),
        (1850, 1450, 740, "返回用户数据"),
        (1450, 1050, 860, "生成 JWT"),
        (1050, 650, 980, "返回 Token"),
        (650, 250, 1080, "进入 Dashboard"),
    ]
    for x1, x2, y, label in msgs:
        arrow(d, (x1, y), (x2, y), 4)
        center_text(d, (min(x1, x2), y - 52, max(x1, x2), y - 10), label, small)
    save(img, "image10_login_sequence.png")


def diagram_realtime_flow():
    img, d = canvas(2300, 1095)
    body = f(48)
    nodes = [
        (90, 180, 390, 300, "监测设备"),
        (560, 180, 860, 300, "数据接收"),
        (1030, 180, 1330, 300, "监测数据表"),
        (1500, 180, 1800, 300, "阈值判断"),
        (1970, 180, 2240, 300, "告警状态"),
        (560, 620, 860, 740, "WebSocket"),
        (1030, 620, 1330, 740, "实时监测页"),
        (1500, 620, 1800, 740, "趋势图表"),
        (1970, 620, 2240, 740, "分页表格"),
    ]
    for b in nodes:
        rect(d, b[:4], b[4], body)
    for a, b in zip(nodes[:5], nodes[1:5]):
        arrow(d, (a[2], 240), (b[0], 240), 4)
    poly_arrow(d, [(2110, 300), (2110, 460), (710, 460), (710, 620)], 4)
    arrow(d, (860, 680), (1030, 680), 4)
    arrow(d, (1330, 680), (1500, 680), 4)
    arrow(d, (1800, 680), (1970, 680), 4)
    save(img, "image11_realtime_flow.png")


def diagram_alert_generation():
    img, d = canvas(2200, 1100)
    body = f(48)
    nodes = [
        (100, 460, 360, 580, "采集数据"),
        (520, 460, 780, 580, "读取规则"),
        (940, 430, 1220, 610, "是否超阈值\n或离线"),
        (1380, 260, 1660, 380, "生成告警"),
        (1800, 260, 2100, 380, "分级通知"),
        (1380, 700, 1660, 820, "记录正常"),
        (1800, 700, 2100, 820, "页面刷新"),
    ]
    for i, b in enumerate(nodes):
        if i == 2:
            # diamond
            cx, cy = (b[0]+b[2])//2, (b[1]+b[3])//2
            pts = [(cx, b[1]), (b[2], cy), (cx, b[3]), (b[0], cy)]
            d.polygon(pts, outline="black", fill="white")
            d.line(pts + [pts[0]], fill="black", width=4)
            center_text(d, b[:4], b[4], body)
        else:
            rect(d, b[:4], b[4], body)
    arrow(d, (360, 520), (520, 520), 4)
    arrow(d, (780, 520), (940, 520), 4)
    arrow(d, (1220, 520), (1380, 320), 4)
    arrow(d, (1660, 320), (1800, 320), 4)
    arrow(d, (1080, 610), (1380, 760), 4)
    arrow(d, (1660, 760), (1800, 760), 4)
    center_text(d, (1260, 380, 1360, 440), "是", f(40))
    center_text(d, (1180, 650, 1280, 710), "否", f(40))
    save(img, "image12_alert_generation.png")


def diagram_ai_sequence():
    img, d = canvas(2200, 1215)
    body, small = f(42), f(33)
    xs = [220, 580, 940, 1300, 1660, 2020]
    labels = ["用户", "AI 助手页", "后端接口", "上下文服务", "数据库", "大模型接口"]
    for x, label in zip(xs, labels):
        rect(d, (x - 130, 80, x + 130, 170), label, body)
        dashed_line(d, (x, 170), (x, 1120), 3, 18)
    msgs = [
        (220, 580, 260, "输入业务问题"),
        (580, 940, 370, "提交问题"),
        (940, 1300, 480, "识别意图"),
        (1300, 1660, 590, "检索库区/设备/告警/工单"),
        (1660, 1300, 700, "返回结构化数据"),
        (1300, 2020, 810, "构造提示词并调用模型"),
        (2020, 1300, 920, "返回分析结果"),
        (1300, 940, 1030, "保存会话消息"),
        (940, 580, 1120, "返回 Markdown 回复"),
    ]
    for x1, x2, y, label in msgs:
        arrow(d, (x1, y), (x2, y), 4)
        center_text(d, (min(x1, x2), y - 48, max(x1, x2), y - 8), label, small)
    save(img, "image13_ai_sequence.png")


def diagram_mini_order():
    img, d = canvas(2200, 1100)
    body = f(48)
    nodes = [
        (110, 180, 390, 300, "工作台"),
        (560, 180, 840, 300, "待接收工单"),
        (1010, 180, 1290, 300, "接收处理"),
        (1460, 180, 1740, 300, "查看详情"),
        (1910, 180, 2170, 300, "现场处置"),
        (1910, 620, 2170, 740, "提交反馈"),
        (1460, 620, 1740, 740, "待验收"),
        (1010, 590, 1290, 770, "验收是否\n通过"),
        (560, 620, 840, 740, "已完成"),
        (110, 620, 390, 740, "退回处理"),
    ]
    for i, b in enumerate(nodes):
        if i == 7:
            cx, cy = (b[0]+b[2])//2, (b[1]+b[3])//2
            pts = [(cx, b[1]), (b[2], cy), (cx, b[3]), (b[0], cy)]
            d.polygon(pts, outline="black", fill="white")
            d.line(pts + [pts[0]], fill="black", width=4)
            center_text(d, b[:4], b[4], body)
        else:
            rect(d, b[:4], b[4], body)
    for a, b in zip(nodes[:5], nodes[1:5]):
        arrow(d, (a[2], 240), (b[0], 240), 4)
    arrow(d, (2040, 300), (2040, 620), 4)
    arrow(d, (1910, 680), (1740, 680), 4)
    arrow(d, (1460, 680), (1290, 680), 4)
    arrow(d, (1010, 680), (840, 680), 4)
    arrow(d, (1010, 680), (390, 680), 4)
    poly_arrow(d, [(250, 620), (250, 460), (1600, 460), (1600, 300)], 4)
    center_text(d, (890, 620, 990, 670), "通过", f(40))
    center_text(d, (650, 560, 780, 610), "不通过", f(40))
    save(img, "image14_mini_order.png")


def main():
    diagram_roles()
    diagram_business_flow()
    diagram_alert_loop()
    diagram_use_case()
    diagram_architecture()
    diagram_deployment()
    diagram_modules()
    diagram_er()
    diagram_class()
    diagram_sequence_login()
    diagram_realtime_flow()
    diagram_alert_generation()
    diagram_ai_sequence()
    diagram_mini_order()
    print(OUT)


def diagram_use_case():
    img, d = canvas(1800, 900)
    body, small = f(54), f(42)
    boundary = (360, 70, 1440, 800)
    rect(d, boundary, "", body, width=4, radius=0)
    center_text(d, (380, 80, 640, 135), "系统边界", small)

    actor(d, 165, 190, "系统管理员", small)
    actor(d, 165, 610, "仓储管理员", small)
    actor(d, 1635, 240, "现场员工", small)
    actor(d, 1635, 650, "大模型服务", small)

    cases = {
        "登录认证": (560, 120, 850, 205),
        "系统管理": (560, 260, 850, 345),
        "库区设备管理": (560, 400, 850, 485),
        "实时监测": (560, 540, 850, 625),
        "阈值规则": (560, 680, 850, 765),
        "告警处理": (980, 190, 1270, 275),
        "工单闭环": (980, 330, 1270, 415),
        "数据分析": (980, 470, 1270, 555),
        "AI 智能问答": (980, 610, 1270, 695),
    }
    for label, xy in cases.items():
        ellipse(d, xy, label, body)

    def left(xy): return (xy[0], (xy[1] + xy[3]) // 2)
    def right(xy): return (xy[2], (xy[1] + xy[3]) // 2)

    # Straight orthogonal links. The diagram intentionally groups related use
    # cases to avoid the dense fan of associations that made the previous image messy.
    line(d, (300, 190), (480, 190), 3)
    line(d, (480, 160), (480, 520), 3)
    for key in ["登录认证", "系统管理", "数据分析"]:
        y = (cases[key][1] + cases[key][3]) // 2
        line(d, (480, y), left(cases[key]), 3)

    line(d, (300, 610), (500, 610), 3)
    line(d, (500, 440), (500, 720), 3)
    for key in ["库区设备管理", "实时监测", "阈值规则", "告警处理", "工单闭环"]:
        y = (cases[key][1] + cases[key][3]) // 2
        target = left(cases[key]) if cases[key][0] < 900 else right(cases[key])
        line(d, (500, y), target, 3)

    line(d, (1500, 240), (1320, 240), 3)
    line(d, (1320, 240), (1320, 375), 3)
    for key in ["告警处理", "工单闭环"]:
        y = (cases[key][1] + cases[key][3]) // 2
        line(d, (1320, y), right(cases[key]), 3)

    line(d, (1500, 650), right(cases["AI 智能问答"]), 3)
    save(img, "image4_use_case.png")


def diagram_deployment():
    img, d = canvas(1800, 760)
    head, body, small = f(48), f(42), f(34)
    cols = [
        (30, 50, 305, 690, "客户端"),
        (435, 50, 925, 690, "应用服务"),
        (1055, 50, 1405, 690, "数据服务"),
        (1515, 50, 1770, 690, "外部服务"),
    ]
    for x1, y1, x2, y2, label in cols:
        dashed_rect(d, (x1, y1, x2, y2), 4, 18)
        center_text(d, (x1, y1 + 14, x2, y1 + 74), label, head)

    rect(d, (75, 185, 260, 305), "Web\n管理端", body)
    rect(d, (75, 455, 260, 575), "微信\n小程序端", body)
    rect(d, (500, 145, 860, 305), "Spring Boot\n后端服务\n端口 8080", body)
    rect(d, (500, 395, 860, 525), "WebSocket\n服务", body)
    rect(d, (500, 585, 860, 675), "AI 上下文构造", small)
    rect(d, (1100, 145, 1360, 255), "MySQL\n数据库", body)
    rect(d, (1100, 335, 1360, 445), "Redis\n缓存", body)
    rect(d, (1100, 525, 1360, 635), "文件与日志", body)
    rect(d, (1535, 530, 1750, 655), "兼容式大模型接口\ndeepseek-v4-pro", small)

    arrow(d, (260, 245), (500, 245), 4)
    center_text(d, (300, 165, 430, 230), "HTTPS\nREST API", small)
    arrow(d, (260, 515), (500, 460), 4)
    center_text(d, (300, 430, 430, 495), "HTTPS\nREST API", small)
    double_arrow(d, (860, 210), (1100, 200), 4)
    double_arrow(d, (860, 265), (1100, 390), 4)
    double_arrow(d, (680, 305), (680, 395), 4)
    poly_arrow(d, [(860, 630), (980, 630), (980, 200), (1100, 200)], 4)
    arrow(d, (860, 630), (1535, 595), 4)
    save(img, "image6_deployment.png")


def diagram_modules():
    img, d = canvas(1900, 760)
    body, small = f(48), f(36)
    rect(d, (570, 35, 1330, 120), "冷链仓储安全管理系统", body)
    rows = [
        (190, 190, "Web 管理端", ["Dashboard", "库区管理", "设备管理", "实时监测", "阈值规则", "告警中心", "工单中心", "数据分析", "系统管理", "AI 智能助手"]),
        (190, 390, "微信小程序端", ["登录", "工作台", "告警查看", "工单处理", "个人中心"]),
        (190, 575, "后端服务", ["认证授权", "业务接口", "实时推送", "数据持久化", "模型接口适配"]),
    ]
    for x, y, label, items in rows:
        rect(d, (x, y, 1710, y + 130), "", body)
        rect(d, (x + 20, y + 28, x + 250, y + 102), label, body, width=3)
        arrow(d, (950, 120), (950, y), 4)
        cols = 5
        bw, bh, gapx, gapy = 240, 44, 24, 22
        start_x, start_y = x + 290, y + 22
        for i, item in enumerate(items):
            row = i // cols
            col = i % cols
            bx = (start_x + col * (bw + gapx), start_y + row * (bh + gapy), start_x + col * (bw + gapx) + bw, start_y + row * (bh + gapy) + bh)
            rect(d, bx, item, small, width=3, radius=8)
    save(img, "image7_modules.png")


def diagram_er():
    img, d = canvas(1800, 960)
    small = f(34)
    entities = {
        "用户": (80, 80, 330, 205, "用户\nuser_id, username\nrole_id, status"),
        "角色": (520, 80, 770, 205, "角色\nrole_id, role_name\npermission_set"),
        "库区": (80, 330, 330, 455, "库区\narea_id, parent_id\nname, path"),
        "设备": (520, 330, 770, 455, "设备\ndevice_id, area_id\ntype, status"),
        "监测数据": (960, 330, 1240, 455, "监测数据\ndata_id, device_id\ntemp, humidity"),
        "阈值规则": (520, 580, 770, 705, "阈值规则\nrule_id, target_id\nmin, max"),
        "告警": (960, 580, 1240, 705, "告警\nalert_id, device_id\nlevel, status"),
        "工单": (1420, 580, 1700, 705, "工单\norder_id, alert_id\nassignee, state"),
        "AI 会话": (960, 800, 1240, 925, "AI 会话\nsession_id, user_id\ncreated_at"),
        "AI 消息": (1420, 800, 1700, 925, "AI 消息\nmessage_id, session_id\nrole, content"),
    }
    for xy in entities.values():
        rect(d, xy[:4], xy[4], small, radius=8)
    double_arrow(d, (330, 142), (520, 142), 4)
    center_text(d, (365, 100, 485, 135), "N:1", small)
    double_arrow(d, (330, 392), (520, 392), 4)
    center_text(d, (365, 350, 485, 385), "1:N", small)
    double_arrow(d, (770, 392), (960, 392), 4)
    center_text(d, (805, 350, 925, 385), "1:N", small)
    double_arrow(d, (645, 455), (645, 580), 4)
    center_text(d, (670, 500, 770, 540), "匹配", small)
    double_arrow(d, (1100, 455), (1100, 580), 4)
    center_text(d, (1125, 500, 1225, 540), "触发", small)
    double_arrow(d, (1240, 642), (1420, 642), 4)
    center_text(d, (1285, 600, 1375, 635), "转化", small)
    double_arrow(d, (1100, 705), (1100, 800), 4)
    double_arrow(d, (1240, 862), (1420, 862), 4)
    double_arrow(d, (330, 142), (960, 862), 4)
    save(img, "image8_er.png")


def diagram_sequence_login():
    img, d = canvas(1800, 820)
    body, small = f(40), f(32)
    xs = [170, 510, 850, 1190, 1530]
    labels = ["用户", "Web 登录页", "AuthController", "UserService", "数据库"]
    for x, label in zip(xs, labels):
        rect(d, (x - 125, 60, x + 125, 140), label, body)
        dashed_line(d, (x, 140), (x, 760), 3, 16)
    msgs = [
        (170, 510, 210, "输入账号密码"),
        (510, 850, 285, "POST /auth/login"),
        (850, 1190, 360, "校验登录参数"),
        (1190, 1530, 435, "查询用户与角色"),
        (1530, 1190, 510, "返回用户数据"),
        (1190, 850, 585, "生成 JWT"),
        (850, 510, 660, "返回 Token"),
        (510, 170, 735, "进入 Dashboard"),
    ]
    for x1, x2, y, label in msgs:
        arrow(d, (x1, y), (x2, y), 4)
        center_text(d, (min(x1, x2), y - 43, max(x1, x2), y - 6), label, small)
    save(img, "image10_login_sequence.png")


def diagram_realtime_flow():
    img, d = canvas(1800, 640)
    body = f(42)
    top = [(60, 105, 250, 190, "监测设备"), (380, 105, 570, 190, "数据接收"), (700, 105, 920, 190, "监测数据表"), (1050, 105, 1240, 190, "阈值判断"), (1370, 105, 1570, 190, "告警状态")]
    bottom = [(380, 405, 570, 490, "WebSocket"), (700, 405, 920, 490, "实时监测页"), (1050, 405, 1240, 490, "趋势图表"), (1370, 405, 1570, 490, "分页表格")]
    for b in top + bottom:
        rect(d, b[:4], b[4], body)
    for a, b in zip(top, top[1:]):
        arrow(d, (a[2], 147), (b[0], 147), 4)
    poly_arrow(d, [(1470, 190), (1470, 300), (475, 300), (475, 405)], 4)
    for a, b in zip(bottom, bottom[1:]):
        arrow(d, (a[2], 447), (b[0], 447), 4)
    save(img, "image11_realtime_flow.png")


def diagram_alert_generation():
    img, d = canvas(1800, 620)
    body = f(42)
    nodes = [
        (60, 250, 250, 335, "采集数据"),
        (370, 250, 560, 335, "读取规则"),
        (700, 220, 930, 365, "是否超阈值\n或离线"),
        (1080, 105, 1270, 190, "生成告警"),
        (1420, 105, 1620, 190, "分级通知"),
        (1080, 425, 1270, 510, "记录正常"),
        (1420, 425, 1620, 510, "页面刷新"),
    ]
    for i, b in enumerate(nodes):
        if i == 2:
            cx, cy = (b[0] + b[2]) // 2, (b[1] + b[3]) // 2
            pts = [(cx, b[1]), (b[2], cy), (cx, b[3]), (b[0], cy)]
            d.polygon(pts, outline="black", fill="white")
            d.line(pts + [pts[0]], fill="black", width=4)
            center_text(d, b[:4], b[4], body)
        else:
            rect(d, b[:4], b[4], body)
    arrow(d, (250, 292), (370, 292), 4)
    arrow(d, (560, 292), (700, 292), 4)
    arrow(d, (930, 292), (1080, 147), 4)
    arrow(d, (1270, 147), (1420, 147), 4)
    arrow(d, (815, 365), (1080, 467), 4)
    arrow(d, (1270, 467), (1420, 467), 4)
    center_text(d, (960, 195, 1040, 240), "是", f(34))
    center_text(d, (920, 380, 1000, 425), "否", f(34))
    save(img, "image12_alert_generation.png")


def diagram_ai_sequence():
    img, d = canvas(1800, 820)
    body, small = f(36), f(30)
    xs = [120, 430, 740, 1050, 1360, 1670]
    labels = ["用户", "AI 助手页", "后端接口", "上下文服务", "数据库", "大模型接口"]
    for x, label in zip(xs, labels):
        rect(d, (x - 110, 60, x + 110, 140), label, body)
        dashed_line(d, (x, 140), (x, 760), 3, 16)
    msgs = [
        (120, 430, 205, "输入业务问题"),
        (430, 740, 270, "提交问题"),
        (740, 1050, 335, "识别意图"),
        (1050, 1360, 400, "检索业务数据"),
        (1360, 1050, 465, "返回结构化数据"),
        (1050, 1670, 530, "调用模型"),
        (1670, 1050, 595, "返回分析结果"),
        (1050, 740, 660, "保存会话消息"),
        (740, 430, 725, "返回 Markdown 回复"),
    ]
    for x1, x2, y, label in msgs:
        arrow(d, (x1, y), (x2, y), 4)
        center_text(d, (min(x1, x2), y - 38, max(x1, x2), y - 5), label, small)
    save(img, "image13_ai_sequence.png")


def diagram_mini_order():
    img, d = canvas(1800, 620)
    body = f(40)
    nodes = [
        (40, 120, 230, 205, "工作台"),
        (360, 120, 570, 205, "待接收工单"),
        (700, 120, 900, 205, "接收处理"),
        (1030, 120, 1220, 205, "查看详情"),
        (1350, 120, 1540, 205, "现场处置"),
        (1350, 405, 1540, 490, "提交反馈"),
        (1030, 405, 1220, 490, "待验收"),
        (700, 375, 900, 520, "验收是否\n通过"),
        (360, 405, 570, 490, "已完成"),
        (40, 405, 230, 490, "退回处理"),
    ]
    for i, b in enumerate(nodes):
        if i == 7:
            cx, cy = (b[0] + b[2]) // 2, (b[1] + b[3]) // 2
            pts = [(cx, b[1]), (b[2], cy), (cx, b[3]), (b[0], cy)]
            d.polygon(pts, outline="black", fill="white")
            d.line(pts + [pts[0]], fill="black", width=4)
            center_text(d, b[:4], b[4], body)
        else:
            rect(d, b[:4], b[4], body)
    for a, b in zip(nodes[:5], nodes[1:5]):
        arrow(d, (a[2], 162), (b[0], 162), 4)
    arrow(d, (1445, 205), (1445, 405), 4)
    arrow(d, (1350, 447), (1220, 447), 4)
    arrow(d, (1030, 447), (900, 447), 4)
    arrow(d, (700, 447), (570, 447), 4)
    arrow(d, (700, 447), (230, 447), 4)
    poly_arrow(d, [(135, 405), (135, 300), (1125, 300), (1125, 205)], 4)
    center_text(d, (600, 390, 690, 430), "通过", f(34))
    center_text(d, (420, 350, 540, 390), "不通过", f(34))
    save(img, "image14_mini_order.png")


def diagram_use_case():
    img, d = canvas(1800, 760)
    body, small = f(48), f(38)
    boundary = (350, 50, 1450, 690)
    rect(d, boundary, "", body, width=4, radius=0)
    center_text(d, (380, 60, 620, 110), "系统边界", small)

    actor(d, 165, 160, "系统管理员", small)
    actor(d, 165, 500, "仓储管理员", small)
    actor(d, 1635, 160, "现场员工", small)
    actor(d, 1635, 500, "大模型服务", small)

    cases = {
        "登录认证": (520, 110, 800, 185),
        "系统管理": (520, 230, 800, 305),
        "库区设备": (520, 350, 800, 425),
        "实时监测": (520, 470, 800, 545),
        "阈值规则": (520, 590, 800, 665),
        "告警处理": (1000, 110, 1280, 185),
        "工单闭环": (1000, 230, 1280, 305),
        "数据分析": (1000, 350, 1280, 425),
        "AI 问答": (1000, 470, 1280, 545),
        "小程序工单": (1000, 590, 1280, 665),
    }
    for label, xy in cases.items():
        ellipse(d, xy, label, body)

    def left(xy): return (xy[0], (xy[1] + xy[3]) // 2)
    def right(xy): return (xy[2], (xy[1] + xy[3]) // 2)

    def assoc_left(start, keys, bus_x):
        line(d, start, (bus_x, start[1]), 3)
        ys = [(cases[k][1] + cases[k][3]) // 2 for k in keys]
        line(d, (bus_x, min(ys)), (bus_x, max(ys)), 3)
        for key in keys:
            y = (cases[key][1] + cases[key][3]) // 2
            target = left(cases[key]) if cases[key][0] < 900 else right(cases[key])
            line(d, (bus_x, y), target, 3)

    def assoc_right(start, keys, bus_x):
        line(d, start, (bus_x, start[1]), 3)
        ys = [(cases[k][1] + cases[k][3]) // 2 for k in keys]
        line(d, (bus_x, min(ys)), (bus_x, max(ys)), 3)
        for key in keys:
            y = (cases[key][1] + cases[key][3]) // 2
            line(d, (bus_x, y), right(cases[key]), 3)

    assoc_left((300, 160), ["登录认证", "系统管理", "数据分析"], 455)
    assoc_left((300, 500), ["库区设备", "实时监测", "阈值规则", "告警处理", "工单闭环", "数据分析"], 475)
    assoc_right((1500, 160), ["告警处理", "工单闭环", "小程序工单"], 1340)
    assoc_right((1500, 500), ["AI 问答"], 1340)
    save(img, "image4_use_case.png")


def diagram_deployment():
    img, d = canvas(1600, 620)
    head, body, small = f(40), f(34), f(28)
    cols = [
        (30, 40, 270, 570, "客户端"),
        (380, 40, 830, 570, "应用服务"),
        (950, 40, 1240, 570, "数据服务"),
        (1350, 40, 1570, 570, "外部服务"),
    ]
    for x1, y1, x2, y2, label in cols:
        dashed_rect(d, (x1, y1, x2, y2), 3, 14)
        center_text(d, (x1, y1 + 10, x2, y1 + 58), label, head)
    rect(d, (70, 150, 230, 245), "Web\n管理端", body)
    rect(d, (70, 385, 230, 480), "微信\n小程序端", body)
    rect(d, (430, 120, 780, 250), "Spring Boot\n后端服务\n8080", body)
    rect(d, (430, 335, 780, 430), "WebSocket\n服务", body)
    rect(d, (430, 485, 780, 555), "AI 上下文构造", small)
    rect(d, (990, 115, 1200, 205), "MySQL\n数据库", body)
    rect(d, (990, 275, 1200, 365), "Redis\n缓存", body)
    rect(d, (990, 440, 1200, 530), "文件与日志", body)
    rect(d, (1365, 430, 1555, 545), "大模型接口\ndeepseek-v4-pro", small)
    arrow(d, (230, 197), (430, 185), 3)
    center_text(d, (265, 120, 375, 178), "HTTPS\nREST API", small)
    arrow(d, (230, 432), (430, 382), 3)
    center_text(d, (265, 350, 375, 408), "HTTPS\nREST API", small)
    double_arrow(d, (780, 165), (990, 160), 3)
    double_arrow(d, (780, 225), (990, 320), 3)
    double_arrow(d, (605, 250), (605, 335), 3)
    poly_arrow(d, [(780, 520), (885, 520), (885, 160), (990, 160)], 3)
    arrow(d, (780, 520), (1365, 488), 3)
    save(img, "image6_deployment.png")


def diagram_modules():
    img, d = canvas(1600, 700)
    body, small = f(38), f(30)
    rect(d, (520, 30, 1080, 100), "冷链仓储安全管理系统", body)
    rows = [
        (120, 165, "Web 管理端", ["Dashboard", "库区管理", "设备管理", "实时监测", "阈值规则", "告警中心", "工单中心", "数据分析", "系统管理", "AI 助手"]),
        (120, 340, "微信小程序端", ["登录", "工作台", "告警查看", "工单处理", "个人中心"]),
        (120, 510, "后端服务", ["认证授权", "业务接口", "实时推送", "数据持久化", "模型适配"]),
    ]
    for x, y, label, items in rows:
        rect(d, (x, y, 1480, y + 115), "", body)
        rect(d, (x + 18, y + 27, x + 205, y + 88), label, body, width=3)
        arrow(d, (800, 100), (800, y), 3)
        cols = 5
        bw, bh, gapx, gapy = 190, 38, 18, 18
        sx, sy = x + 235, y + 20
        for i, item in enumerate(items):
            row = i // cols
            col = i % cols
            bx = (sx + col * (bw + gapx), sy + row * (bh + gapy), sx + col * (bw + gapx) + bw, sy + row * (bh + gapy) + bh)
            rect(d, bx, item, small, width=3, radius=7)
    save(img, "image7_modules.png")


def diagram_er():
    img, d = canvas(1600, 700)
    body, rel = f(38), f(28)
    entities = {
        "用户": (70, 70, 250, 140),
        "角色": (430, 70, 610, 140),
        "库区": (70, 250, 250, 320),
        "设备": (430, 250, 610, 320),
        "监测数据": (760, 250, 990, 320),
        "阈值规则": (430, 430, 650, 500),
        "告警": (760, 430, 940, 500),
        "工单": (1110, 430, 1290, 500),
        "AI 会话": (760, 580, 940, 650),
        "AI 消息": (1110, 580, 1290, 650),
    }
    for label, xy in entities.items():
        rect(d, xy, label, body, radius=8)
    double_arrow(d, (250, 105), (430, 105), 3); center_text(d, (290, 70, 390, 100), "N:1", rel)
    double_arrow(d, (250, 285), (430, 285), 3); center_text(d, (290, 250, 390, 280), "1:N", rel)
    double_arrow(d, (610, 285), (760, 285), 3); center_text(d, (645, 250, 725, 280), "1:N", rel)
    double_arrow(d, (520, 320), (520, 430), 3); center_text(d, (545, 360, 620, 395), "匹配", rel)
    double_arrow(d, (850, 320), (850, 430), 3); center_text(d, (875, 360, 950, 395), "触发", rel)
    double_arrow(d, (940, 465), (1110, 465), 3); center_text(d, (985, 430, 1070, 460), "转化", rel)
    double_arrow(d, (850, 500), (850, 580), 3)
    double_arrow(d, (940, 615), (1110, 615), 3)
    double_arrow(d, (250, 105), (760, 615), 3)
    save(img, "image8_er.png")


def diagram_class():
    img, d = canvas(1600, 700)
    body = f(34)
    classes = {
        "User": (70, 70, 250, 145),
        "Area": (400, 70, 580, 145),
        "Device": (730, 70, 930, 145),
        "SensorData": (1080, 70, 1320, 145),
        "Alert": (250, 335, 430, 410),
        "WorkOrder": (580, 335, 820, 410),
        "AiSession": (960, 335, 1200, 410),
        "AiMessage": (960, 555, 1200, 630),
    }
    for label, xy in classes.items():
        rect(d, xy, label, body, radius=0)
    double_arrow(d, (250, 107), (400, 107), 3)
    double_arrow(d, (580, 107), (730, 107), 3)
    double_arrow(d, (930, 107), (1080, 107), 3)
    double_arrow(d, (830, 145), (340, 335), 3)
    double_arrow(d, (430, 372), (580, 372), 3)
    double_arrow(d, (1080, 410), (1080, 555), 3)
    double_arrow(d, (250, 145), (1000, 335), 3)
    save(img, "image9_class.png")


if __name__ == "__main__":
    main()
