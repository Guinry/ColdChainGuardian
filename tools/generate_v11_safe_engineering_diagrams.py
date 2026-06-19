from __future__ import annotations

import importlib.util
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont


BASE = Path(r"C:\Users\23869\Desktop\ColdChainGuardian\tools\generate_v11_clean_engineering_diagrams.py")
OUT = Path(r"C:\Users\23869\Desktop\ColdChainGuardian\output\doc\qa_v11\diagrams")
spec = importlib.util.spec_from_file_location("clean_diagrams", BASE)
clean = importlib.util.module_from_spec(spec)
assert spec.loader is not None
spec.loader.exec_module(clean)


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    return clean.f(size, bold)


def text_box(d: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], text: str, ft: ImageFont.FreeTypeFont, bold: bool = False) -> None:
    clean.center_text(d, xy, text, ft)


def box(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    label: str,
    size: int = 54,
    bold: bool = False,
    width: int = 5,
    radius: int = 14,
) -> None:
    clean.rect(d, xy, label, font(size, bold), width=width, radius=radius)


def ellipse(
    d: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    label: str,
    size: int = 54,
    width: int = 5,
) -> None:
    clean.ellipse(d, xy, label, font(size), width=width)


def diamond(
    d: ImageDraw.ImageDraw,
    cx: int,
    cy: int,
    w: int,
    h: int,
    label: str,
    size: int = 48,
) -> tuple[int, int, int, int]:
    return clean.diamond(d, cx, cy, w, h, label, font(size), width=5)


def arrow(d: ImageDraw.ImageDraw, pts: list[tuple[int, int]], width: int = 5, head: int = 24) -> None:
    clean.arrow(d, pts, width=width, head=head)


def line(d: ImageDraw.ImageDraw, pts: list[tuple[int, int]], width: int = 5) -> None:
    clean.line(d, pts, width=width)


def label(d: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], text: str, size: int = 42) -> None:
    x1, y1, x2, y2 = xy
    d.rectangle((x1, y1, x2, y2), fill="white")
    clean.center_text(d, xy, text, font(size))


def actor(d: ImageDraw.ImageDraw, cx: int, cy: int, name: str) -> None:
    clean.actor(d, cx, cy, name, font(46))


def crop_save(img: Image.Image, name: str, margin: int = 40) -> None:
    bg = Image.new("RGB", img.size, "white")
    diff = ImageChops.difference(img, bg)
    bbox = diff.getbbox()
    if bbox:
        x1, y1, x2, y2 = bbox
        x1 = max(0, x1 - margin)
        y1 = max(0, y1 - margin)
        x2 = min(img.width, x2 + margin)
        y2 = min(img.height, y2 + margin)
        img = img.crop((x1, y1, x2, y2))
    img.save(OUT / name, quality=95, dpi=(300, 300))


def diagram_roles_permissions() -> None:
    img, d = clean.canvas(2200, 980)
    rows = [
        (110, "系统管理员", ["用户与角色", "系统配置", "全局数据"]),
        (330, "仓储管理员", ["库区设备", "监测告警", "工单管理"]),
        (550, "现场员工", ["移动工单", "处置反馈", "个人信息"]),
        (770, "大模型服务", ["数据查询", "风险分析", "辅助建议"]),
    ]
    for y, role, perms in rows:
        box(d, (90, y, 430, y + 105), role, 56, True)
        line(d, [(430, y + 52), (560, y + 52)], 5)
        group = (560, y - 20, 2100, y + 125)
        clean.plain_rect(d, group, width=4)
        w = 430
        x = 650
        for perm in perms:
            box(d, (x, y + 15, x + w, y + 92), perm, 52, False, width=4)
            if x > 650:
                arrow(d, [(x - 55, y + 53), (x, y + 53)], width=4, head=18)
            x += 475
    crop_save(img, "image1_roles_permissions.png")


def diagram_business_flow() -> None:
    img, d = clean.canvas(2300, 1180)
    top = [(120, 130, "设备采集"), (520, 130, "数据接入"), (920, 130, "阈值判断"), (1320, 130, "告警生成"), (1720, 130, "实时推送")]
    mid = [(320, 480, "告警研判"), (720, 480, "工单派发"), (1120, 480, "现场处理"), (1520, 480, "验收关闭")]
    bottom = [(320, 820, "闭环记录"), (720, 820, "趋势统计"), (1120, 820, "风险分析"), (1520, 820, "复盘优化")]
    def draw_row(row):
        boxes = []
        for x, y, t in row:
            b = (x, y, x + 260, y + 95)
            box(d, b, t, 52)
            boxes.append(b)
        for a, b in zip(boxes, boxes[1:]):
            arrow(d, [(a[2], (a[1] + a[3]) // 2), (b[0], (b[1] + b[3]) // 2)], 5)
        return boxes
    top_b = draw_row(top)
    mid_b = draw_row(mid)
    bottom_b = draw_row(bottom)
    arrow(d, [(top_b[-1][2], 178), (2060, 178), (2060, 390), (450, 390), (450, mid_b[0][1])], 5)
    for a, b in zip(mid_b, bottom_b):
        arrow(d, [((a[0] + a[2]) // 2, a[3]), ((b[0] + b[2]) // 2, b[1])], 5)
    crop_save(img, "image2_business_flow.png")


def diagram_alert_loop() -> None:
    img, d = clean.canvas(2200, 900)
    top = [(90, 120, "告警生成"), (450, 120, "管理研判"), (810, 120, "转为工单"), (1170, 120, "员工接收"), (1530, 120, "现场处理")]
    lower = [(450, 520, "误报关闭"), (810, 520, "直接解决"), (1170, 520, "闭环归档"), (1530, 520, "管理验收")]
    boxes = {}
    for x, y, t in top + lower:
        boxes[t] = (x, y, x + 260, y + 95)
        box(d, boxes[t], t, 52)
    for a, b in zip([t for _, _, t in top], [t for _, _, t in top][1:]):
        ba, bb = boxes[a], boxes[b]
        arrow(d, [(ba[2], 168), (bb[0], 168)], 5)
    arrow(d, [(580, 215), (580, 520)], 5)
    arrow(d, [(940, 215), (940, 520)], 5)
    arrow(d, [(1660, 215), (1660, 520)], 5)
    arrow(d, [(1530, 568), (1430, 568)], 5)
    arrow(d, [(1170, 568), (1070, 568)], 5)
    arrow(d, [(940, 615), (940, 740), (1660, 740), (1660, 615)], 5)
    crop_save(img, "image3_alert_loop.png")


def diagram_use_case() -> None:
    img, d = clean.canvas(2400, 1160)
    clean.dashed_rect(d, (440, 80, 1960, 1050), width=5, dash=34)
    d.text((500, 115), "系统边界", font=font(46), fill="black")
    left_cases = [(690, 180, "登录认证"), (690, 340, "系统管理"), (690, 500, "库区设备"), (690, 660, "实时监测"), (690, 820, "阈值规则")]
    right_cases = [(1370, 260, "告警处理"), (1370, 460, "工单闭环"), (1370, 660, "数据分析"), (1370, 860, "AI 智能问答")]
    for x, y, t in left_cases + right_cases:
        ellipse(d, (x, y, x + 390, y + 105), t, 54)
    actor(d, 200, 275, "系统管理员")
    actor(d, 200, 780, "仓储管理员")
    actor(d, 2180, 510, "现场员工")
    actor(d, 2180, 900, "大模型服务")
    for y in [232, 392]:
        line(d, [(290, 275), (440, 275), (440, y), (690, y)], 4)
    for y in [552, 712, 872]:
        line(d, [(290, 780), (440, 780), (440, y), (690, y)], 4)
    for y in [312, 512]:
        line(d, [(1760, y), (1960, y), (1960, 510), (2090, 510)], 4)
    line(d, [(1760, 912), (1960, 912), (1960, 900), (2090, 900)], 4)
    arrow(d, [(1080, 552), (1230, 552), (1230, 712), (1370, 712)], 4)
    arrow(d, [(1080, 712), (1230, 712), (1230, 312), (1370, 312)], 4)
    arrow(d, [(1565, 365), (1565, 460)], 4)
    crop_save(img, "image4_use_case.png")


def diagram_architecture() -> None:
    img, d = clean.canvas(2200, 920)
    rows = [
        (80, "表现层", ["Web 管理端", "微信小程序端", "PC 浏览器"]),
        (290, "接入层", ["REST API", "JWT 认证", "WebSocket 推送"]),
        (500, "业务层", ["库区设备", "实时监测", "告警工单", "数据分析", "AI 助手"]),
        (710, "数据层", ["MySQL", "Redis", "日志审计", "大模型接口"]),
    ]
    for y, layer, nodes in rows:
        clean.plain_rect(d, (80, y, 2120, y + 145), width=5)
        box(d, (110, y + 28, 330, y + 117), layer, 56, True, width=0)
        x0 = 430
        gap = 35
        w = (2050 - x0 - gap * (len(nodes) - 1)) // len(nodes)
        for i, node in enumerate(nodes):
            x = x0 + i * (w + gap)
            box(d, (x, y + 35, x + w, y + 110), node, 50, False, width=4)
    for x in [760, 1120, 1480]:
        arrow(d, [(x, 225), (x, 290)], 5)
        arrow(d, [(x, 435), (x, 500)], 5)
        arrow(d, [(x, 645), (x, 710)], 5)
    crop_save(img, "image5_architecture.png")


def diagram_deployment() -> None:
    img, d = clean.canvas(2250, 1060)
    groups = [(80, 100, 470, 900, "客户端"), (610, 100, 1110, 900, "应用服务"), (1250, 100, 1670, 900, "数据服务"), (1810, 100, 2170, 900, "外部服务")]
    for x1, y1, x2, y2, name in groups:
        clean.dashed_rect(d, (x1, y1, x2, y2), width=5, dash=30)
        d.text((x1 + 120, y1 + 20), name, font=font(54, True), fill="black")
    nodes = {
        "web": (140, 270, 410, 380, "Web\n管理端"),
        "mini": (140, 590, 410, 700, "微信\n小程序端"),
        "boot": (690, 260, 1030, 390, "Spring Boot\n后端 8080"),
        "ws": (690, 570, 1030, 700, "WebSocket\n服务"),
        "ctx": (690, 760, 1030, 880, "AI 上下文\n构造"),
        "mysql": (1320, 260, 1600, 390, "MySQL\n数据库"),
        "redis": (1320, 570, 1600, 700, "Redis\n缓存"),
        "log": (1320, 760, 1600, 880, "文件与日志"),
        "llm": (1835, 420, 2155, 650, "兼容式\n大模型接口\n模型服务"),
    }
    for b in nodes.values():
        box(d, b[:4], b[4], 48)
    arrow(d, [(410, 325), (690, 325)], 5)
    arrow(d, [(410, 645), (690, 635)], 5)
    arrow(d, [(1030, 325), (1320, 325)], 5)
    arrow(d, [(1030, 635), (1320, 635)], 5)
    arrow(d, [(1030, 820), (1320, 820)], 5)
    arrow(d, [(1030, 820), (1165, 820), (1165, 960), (1750, 960), (1750, 535), (1848, 535)], 5)
    crop_save(img, "image6_deployment.png")


def diagram_modules() -> None:
    img, d = clean.canvas(2400, 1000)
    box(d, (760, 70, 1640, 170), "冷链仓储安全管理系统", 62, True)
    columns = [
        (120, "Web 管理端", ["Dashboard 与全局检索", "库区、设备维护", "实时监测与阈值规则", "告警工单与数据分析", "系统管理与 AI 助手"]),
        (650, "微信小程序端", ["登录认证与工作台", "告警查看与任务提醒", "工单接收与处置", "反馈提交与个人中心"]),
        (1180, "后端服务", ["认证授权", "业务接口", "实时推送", "数据持久化", "日志审计"]),
        (1710, "智能分析", ["上下文构造", "数据摘要", "风险解释", "处置建议"]),
    ]
    line(d, [(1200, 170), (1200, 270), (350, 270), (1970, 270)], 5)
    for x, title, items in columns:
        arrow(d, [(x + 240, 270), (x + 240, 335)], 5)
        box(d, (x, 335, x + 480, 430), title, 54, True)
        clean.plain_rect(d, (x, 430, x + 480, 870), width=5)
        y = 465
        for item in items:
            d.text((x + 35, y), item, font=font(42), fill="black")
            y += 75
    crop_save(img, "image7_modules.png")


def diagram_er() -> None:
    img, d = clean.canvas(2300, 1080)
    nodes = {
        "user": (100, 90, 420, 200, "用户"),
        "role": (620, 90, 940, 200, "角色"),
        "rule": (1160, 90, 1500, 200, "阈值规则"),
        "area": (100, 455, 420, 565, "库区"),
        "device": (620, 455, 940, 565, "设备"),
        "data": (1160, 455, 1500, 565, "监测数据"),
        "alert": (1710, 455, 2030, 565, "告警"),
        "ai": (620, 820, 940, 930, "AI 会话"),
        "msg": (1160, 820, 1500, 930, "AI 消息"),
        "order": (1710, 820, 2030, 930, "工单"),
    }
    for b in nodes.values():
        box(d, b[:4], b[4], 62, True, width=6)
    def edge(a: str, b: str, pts: list[tuple[int, int]], t: str, tx: int, ty: int):
        line(d, pts, 5)
        label(d, (tx - 58, ty - 30, tx + 58, ty + 30), t, 42)
    edge("user", "role", [(420, 145), (620, 145)], "N:1", 520, 145)
    edge("area", "device", [(420, 510), (620, 510)], "1:N", 520, 510)
    edge("device", "data", [(940, 510), (1160, 510)], "1:N", 1050, 510)
    edge("data", "alert", [(1500, 510), (1710, 510)], "触发", 1605, 510)
    edge("alert", "order", [(1870, 565), (1870, 820)], "1:1", 1870, 700)
    edge("ai", "msg", [(940, 875), (1160, 875)], "1:N", 1050, 875)
    edge("user", "ai", [(260, 200), (260, 875), (620, 875)], "1:N", 440, 875)
    edge("rule", "alert", [(1330, 200), (1330, 325), (1870, 325), (1870, 455)], "匹配", 1600, 325)
    crop_save(img, "image8_er.png")


def diagram_class() -> None:
    img, d = clean.canvas(2300, 1040)
    nodes = {
        "user": (100, 80, 470, 210, "User\nlogin()"),
        "area": (590, 80, 960, 210, "Area\nbuildTree()"),
        "device": (1080, 80, 1450, 210, "Device\nbindArea()"),
        "data": (1570, 80, 1960, 210, "SensorData\nsave()"),
        "rule": (590, 430, 960, 560, "Rule\nmatch()"),
        "alert": (1080, 430, 1450, 560, "Alert\nconfirm()"),
        "order": (1570, 430, 1960, 560, "WorkOrder\ndispatch()"),
        "session": (590, 780, 960, 910, "AiSession\ncreate()"),
        "message": (1080, 780, 1450, 910, "AiMessage\nappend()"),
    }
    for b in nodes.values():
        box(d, b[:4], b[4], 50, True, width=5)
    line(d, [(470, 145), (590, 145)], 4)
    line(d, [(960, 145), (1080, 145)], 4)
    line(d, [(1450, 145), (1570, 145)], 4)
    line(d, [(1265, 210), (1265, 430)], 4)
    line(d, [(960, 495), (1080, 495)], 4)
    line(d, [(1450, 495), (1570, 495)], 4)
    line(d, [(285, 210), (285, 845), (590, 845)], 4)
    line(d, [(960, 845), (1080, 845)], 4)
    crop_save(img, "image9_class.png")


def diagram_login_sequence() -> None:
    img, d = clean.canvas(2200, 900)
    xs = [200, 600, 1000, 1400, 1800]
    titles = ["用户", "Web 登录页", "AuthController", "UserService", "数据库"]
    for x, t in zip(xs, titles):
        box(d, (x - 145, 70, x + 145, 145), t, 46)
        clean.dashed_line(d, (x, 145), (x, 760), width=3, dash=22)
    msgs = [(0, 1, 220, "输入账号密码"), (1, 2, 300, "POST /auth/login"), (2, 3, 380, "校验参数"), (3, 4, 460, "查询用户角色"), (4, 3, 540, "返回用户数据"), (3, 2, 620, "生成 JWT"), (2, 1, 700, "返回 Token"), (1, 0, 760, "进入 Dashboard")]
    for a, b, y, t in msgs:
        arrow(d, [(xs[a], y), (xs[b], y)], 4, 20)
        label(d, (min(xs[a], xs[b]) + 20, y - 46, max(xs[a], xs[b]) - 20, y - 12), t, 36)
    crop_save(img, "image10_login_sequence.png")


def diagram_realtime_flow() -> None:
    img, d = clean.canvas(2400, 780)
    top = [(80, 100, "监测设备"), (460, 100, "数据接收"), (840, 100, "监测数据表"), (1220, 100, "阈值判断"), (1600, 100, "告警状态")]
    bottom = [(1600, 455, "分页表格"), (1220, 455, "趋势图表"), (840, 455, "实时监测页"), (420, 455, "WebSocket")]
    boxes = []
    for x, y, t in top + bottom:
        w = 320 if t == "WebSocket" else 300
        b = (x, y, x + w, y + 105)
        box(d, b, t, 46 if t == "WebSocket" else 50)
        boxes.append(b)
    for a, b in zip(boxes[:5], boxes[1:5]):
        arrow(d, [(a[2], 152), (b[0], 152)], 5)
    arrow(d, [(1750, 205), (1750, 455)], 5)
    for a, b in zip(boxes[5:], boxes[6:]):
        arrow(d, [(a[0], 507), (b[2], 507)], 5)
    arrow(d, [(580, 455), (580, 320), (1600, 320), (1600, 152)], 5)
    crop_save(img, "image11_realtime_flow.png")


def diagram_alert_generation() -> None:
    img, d = clean.canvas(2200, 700)
    box(d, (120, 290, 390, 390), "采集数据", 52)
    box(d, (540, 290, 810, 390), "读取规则", 52)
    diamond(d, 1050, 340, 270, 170, "是否超过\n阈值", 44)
    box(d, (1350, 130, 1620, 230), "生成告警", 52)
    box(d, (1770, 130, 2040, 230), "分级通知", 52)
    box(d, (1350, 450, 1620, 550), "记录正常", 52)
    box(d, (1770, 450, 2040, 550), "页面刷新", 52)
    arrow(d, [(390, 340), (540, 340)], 5)
    arrow(d, [(810, 340), (915, 340)], 5)
    arrow(d, [(1185, 340), (1240, 340), (1240, 180), (1350, 180)], 5)
    label(d, (1220, 210, 1285, 260), "是", 40)
    arrow(d, [(1620, 180), (1770, 180)], 5)
    arrow(d, [(1050, 425), (1050, 500), (1350, 500)], 5)
    label(d, (1085, 445, 1150, 495), "否", 40)
    arrow(d, [(1620, 500), (1770, 500)], 5)
    crop_save(img, "image12_alert_generation.png")


def diagram_ai_sequence() -> None:
    img, d = clean.canvas(2300, 900)
    xs = [130, 470, 810, 1150, 1490, 1830, 2170]
    titles = ["用户", "AI 助手页", "后端接口", "上下文服务", "数据库", "大模型接口", "消息记录"]
    for x, t in zip(xs, titles):
        box(d, (x - 120, 70, x + 120, 145), t, 42)
        clean.dashed_line(d, (x, 145), (x, 760), width=3, dash=22)
    msgs = [(0, 1, 215, "输入问题"), (1, 2, 290, "提交问题"), (2, 3, 365, "构造任务"), (3, 4, 440, "查询业务数据"), (4, 3, 515, "返回上下文"), (3, 5, 590, "调用模型接口"), (5, 3, 665, "返回结果"), (3, 6, 740, "保存消息"), (2, 1, 810, "返回答复")]
    for a, b, y, t in msgs:
        arrow(d, [(xs[a], y), (xs[b], y)], 4, 20)
        label(d, (min(xs[a], xs[b]) + 10, y - 42, max(xs[a], xs[b]) - 10, y - 10), t, 34)
    crop_save(img, "image13_ai_sequence.png")


def diagram_mini_order() -> None:
    img, d = clean.canvas(2400, 760)
    top = [(80, 100, "工作台"), (450, 100, "待接收工单"), (820, 100, "接收处理"), (1190, 100, "查看详情"), (1560, 100, "现场处置")]
    lower = [(1560, 460, "提交反馈"), (1190, 460, "待验收"), (820, 460, "验收通过"), (450, 460, "已完成"), (80, 460, "退回处理")]
    for x, y, t in top + lower:
        box(d, (x, y, x + 290, y + 105), t, 50)
    for a, b in zip(top, top[1:]):
        arrow(d, [(a[0] + 290, 152), (b[0], 152)], 5)
    arrow(d, [(1705, 205), (1705, 460)], 5)
    for a, b in zip(lower, lower[1:]):
        arrow(d, [(a[0], 512), (b[0] + 290, 512)], 5)
    label(d, (760, 390, 880, 435), "通过", 36)
    label(d, (320, 390, 470, 435), "不通过", 36)
    crop_save(img, "image14_mini_order.png")


def contact_sheet() -> None:
    clean.contact_sheet()


def main() -> None:
    diagram_roles_permissions()
    diagram_business_flow()
    diagram_alert_loop()
    diagram_use_case()
    diagram_architecture()
    diagram_deployment()
    diagram_modules()
    diagram_er()
    diagram_class()
    diagram_login_sequence()
    diagram_realtime_flow()
    diagram_alert_generation()
    diagram_ai_sequence()
    diagram_mini_order()
    contact_sheet()


if __name__ == "__main__":
    main()
