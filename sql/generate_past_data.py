"""
Generate past hotel data with late checkout fee policy:
- Late >= 30 min: phu_phi_tra_muon = 50% room price per late day
- tienPhong includes late days (actual total nights)
"""
import random
from datetime import datetime, timedelta

random.seed(42)

ROOMS = {
    'P101': ('SINGLE', 300000), 'P102': ('DOUBLE', 500000), 'P103': ('TWIN', 600000),
    'P104': ('TRIPLE', 700000), 'P105': ('FAMILY', 1000000),
    'P201': ('SINGLE', 300000), 'P202': ('DOUBLE', 500000), 'P203': ('TWIN', 600000),
    'P204': ('TRIPLE', 700000), 'P205': ('FAMILY', 1000000),
    'P301': ('SINGLE', 300000), 'P302': ('DOUBLE', 500000), 'P303': ('TWIN', 600000),
    'P304': ('TRIPLE', 700000), 'P305': ('FAMILY', 1000000),
    'P401': ('SINGLE', 300000), 'P402': ('DOUBLE', 500000), 'P403': ('TWIN', 600000),
    'P404': ('TRIPLE', 700000), 'P405': ('FAMILY', 1000000),
    'P501': ('SINGLE', 300000), 'P502': ('DOUBLE', 500000), 'P503': ('TWIN', 600000),
    'P504': ('TRIPLE', 700000), 'P505': ('FAMILY', 1000000),
}
ROOM_KEYS = list(ROOMS.keys())

SERVICES = {
    'DV001': ('Nuoc suoi', 15000), 'DV002': ('Com chien hai san', 80000),
    'DV003': ('Giat ui', 100000), 'DV004': ('Nuoc ngot lon', 20000),
    'DV005': ('Bia Heineken', 30000), 'DV006': ('Mi ly', 20000),
    'DV007': ('Thue xe may', 100000), 'DV008': ('Dua don san bay', 200000),
    'DV009': ('Bo bit tet', 200000), 'DV010': ('Bua toi tai phong', 300000),
    'DV011': ('Spa thu gian', 400000), 'DV012': ('Karaoke VIP', 400000),
}
SERVICE_KEYS = list(SERVICES.keys())

STAFF = ['LUCIA003','LUCIA004','LUCIA005','LUCIA006','LUCIA007',
         'LUCIA008','LUCIA009','LUCIA010','LUCIA011','LUCIA012']
CUSTOMERS = [f'KH{i:03d}' for i in range(1, 101)]
VAT = 0.1
CUTOFF = datetime(2026, 5, 26)

# ===== HELPERS =====
dat_phong_rows, ctdp_rows, dvsd_rows, hoa_don_rows, cthd_rows = [], [], [], [], []
dp_idx = ctdp_idx = hd_idx = cthd_idx = 1
room_occupied = {}

def to_date(d):
    return d.date() if isinstance(d, datetime) else d

def is_room_available(r, ci, co):
    d = to_date(ci)
    end = to_date(co)
    while d < end:
        if (r, d) in room_occupied: return False
        d += timedelta(days=1)
    return True

def mark_room_occupied(r, ci, co):
    d = to_date(ci)
    end = to_date(co)
    while d < end:
        room_occupied[(r, d)] = True
        d += timedelta(days=1)

def random_time(base, hr=(8,20)):
    return base.replace(hour=random.randint(hr[0],hr[1]), minute=random.randint(0,59), second=0)

def fmt_dt(dt): return dt.strftime('%Y-%m-%d %H:%M:%S.000')
def fmt_date(dt): return dt.strftime('%Y-%m-%d')

def calc_late_days(planned_out_dt, actual_out_dt):
    """Calculate number of late days. >= 30 min past planned = 1 late day."""
    if actual_out_dt <= planned_out_dt:
        return 0
    late_mins = (actual_out_dt - planned_out_dt).total_seconds() / 60
    if late_mins < 30:
        return 0
    return int((late_mins - 30 + 1440) // 1440)

# ===== BUILD BOOKINGS =====
booking_configs = []
for month in [3, 4, 5]:
    num = {3: 65, 4: 80, 5: 55}[month]
    for _ in range(num):
        day = random.randint(1, 16 if month == 5 else 28)
        try: bd = datetime(2026, month, day)
        except: bd = datetime(2026, month, 28)

        ci = bd + timedelta(days=random.randint(0, 7))
        stay = random.choices([1,2,3,4,5], weights=[20,35,25,12,8])[0]
        planned_co = ci + timedelta(days=stay)
        planned_co_dt = planned_co.replace(hour=12, minute=0, second=0)

        btype = random.choices(
            ['checkout','late_checkout','huy_som','huy_muon'],
            weights=[60, 20, 10, 10]
        )[0]

        # Generate actual checkout time
        actual_co_dt = planned_co_dt
        late_extra_days = 0
        if btype == 'late_checkout':
            scenario = random.choices(
                ['same_day_short', 'same_day_long', '1day', '2days'],
                weights=[35, 25, 30, 10]
            )[0]
            if scenario == 'same_day_short':
                # 30 min - 3 hours late (same day)
                mins = random.randint(30, 180)
                actual_co_dt = planned_co_dt + timedelta(minutes=mins)
            elif scenario == 'same_day_long':
                # 3-8 hours late (same day, checkout in evening)
                actual_co_dt = planned_co_dt.replace(
                    hour=random.randint(15, 20),
                    minute=random.randint(0, 59))
            elif scenario == '1day':
                # 1 day late
                late_extra_days = 1
                next_day = planned_co + timedelta(days=1)
                actual_co_dt = next_day.replace(
                    hour=random.randint(9, 15),
                    minute=random.randint(0, 59), second=0)
            else:
                # 2 days late
                late_extra_days = 2
                next2 = planned_co + timedelta(days=2)
                actual_co_dt = next2.replace(
                    hour=random.randint(9, 15),
                    minute=random.randint(0, 59), second=0)

        # Room occupied until actual checkout date
        occupy_end = actual_co_dt.date() + timedelta(days=1)

        if actual_co_dt >= CUTOFF: continue
        if planned_co >= CUTOFF and btype == 'checkout': continue

        booking_configs.append({
            'bd': bd, 'ci': ci, 'stay': stay,
            'planned_co_dt': planned_co_dt,
            'actual_co_dt': actual_co_dt,
            'occupy_end': occupy_end,
            'late_extra_days': late_extra_days,
            'num_rooms': random.choices([1,2,3], weights=[55,30,15])[0],
            'type': btype,
        })

booking_configs.sort(key=lambda x: x['bd'])
booking_configs = booking_configs[:200]

# ===== GENERATE SQL =====
for cfg in booking_configs:
    ma_dat = f'DP{dp_idx:03d}'
    ma_kh, ma_nv = random.choice(CUSTOMERS), random.choice(STAFF)
    ngay_dat = random_time(cfg['bd'])
    ci_dt = cfg['ci'].replace(hour=14, minute=0, second=0)
    planned_co_dt = cfg['planned_co_dt']
    actual_co_dt = cfg['actual_co_dt']
    stay = cfg['stay']

    # Select available rooms
    selected = []
    shuffled = ROOM_KEYS.copy(); random.shuffle(shuffled)
    for r in shuffled:
        if len(selected) >= cfg['num_rooms']: break
        if is_room_available(r, cfg['ci'], cfg['occupy_end']):
            if cfg['type'] in ('checkout','late_checkout'):
                mark_room_occupied(r, cfg['ci'], cfg['occupy_end'])
            selected.append(r)
    if not selected: continue

    trang_thai = 'DA_CHECKOUT' if cfg['type'] in ('checkout','late_checkout') else 'DA_HUY'
    co_db = planned_co_dt # BẮT BUỘC LÀ PLANNED, VÌ ACTUAL ĐÃ LƯU VÀO HOADON.NGAYTAOHD

    dat_phong_rows.append(
        f"('{ma_dat}', '{fmt_dt(ngay_dat)}', '{ma_kh}', "
        f"'{fmt_dt(ci_dt)}', '{fmt_dt(co_db)}', N'{trang_thai}')")

    room_ctdps = []
    total_coc = total_tp = total_dv = total_late = 0

    for mp in selected:
        ma_ctdp = f'CTDP{ctdp_idx:04d}'
        loai, gia = ROOMS[mp]
        cap = {'SINGLE':1,'DOUBLE':2,'TWIN':2,'TRIPLE':3,'FAMILY':4}
        sn = random.randint(1, cap[loai])
        total_coc += gia

        ctdp_rows.append(f"('{ma_ctdp}', '{mp}', '{ma_dat}', {int(gia)}, {sn})")

        # Late days for this room
        n_late = calc_late_days(planned_co_dt, actual_co_dt)

        # tienPhong = gia * (planned nights + late days)
        actual_nights = stay + n_late
        tp_room = gia * actual_nights
        total_tp += tp_room

        # phu_phi_tra_muon = 50% * gia * số ngày trễ
        late_fee_room = int(gia * 0.5 * n_late)
        total_late += late_fee_room

        # Services
        dv_room = 0
        if cfg['type'] in ('checkout','late_checkout') and random.random() < 0.6:
            for dv_id in random.sample(SERVICE_KEYS, min(random.randint(1,3), len(SERVICE_KEYS))):
                _, dv_gia = SERVICES[dv_id]
                sl = random.randint(1, 3)
                sd = cfg['ci'] + timedelta(days=random.randint(0, max(0, stay-1)))
                dv_room += dv_gia * sl
                dvsd_rows.append(
                    f"('{dv_id}', '{ma_ctdp}', '{fmt_date(sd)}', {sl}, {int(dv_gia)}, 1)")
        total_dv += dv_room

        room_ctdps.append({
            'ctdp': ma_ctdp, 'tp': tp_room, 'dv': dv_room,
            'nights': actual_nights, 'late': late_fee_room})
        ctdp_idx += 1

    # HoaDon
    ma_hd = f'HD{hd_idx:03d}'
    if cfg['type'] in ('checkout','late_checkout'):
        dt_raw = (total_tp + total_dv + total_late) * (1 + VAT)
        tt_raw = max(0, dt_raw - total_coc)
        n_late = calc_late_days(planned_co_dt, actual_co_dt)
        gc = 'NULL'
        if n_late > 0:
            gc = f"N'Tra muon {n_late} ngay - phu thu 50%'"

        hoa_don_rows.append(
            f"('{ma_hd}', '{ma_dat}', '{ma_nv}', '{fmt_dt(actual_co_dt)}', "
            f"{int(total_tp)}, {int(total_dv)}, {int(total_coc)}, {VAT}, "
            f"{int(tt_raw)}, {int(dt_raw)}, N'HOA_DON_PHONG', N'DA_THANH_TOAN', "
            f"{gc}, 0, {int(total_late)})")

        for rc in room_ctdps:
            cthd_rows.append(
                f"('{f'CTHD{cthd_idx:04d}'}', '{ma_hd}', '{rc['ctdp']}', "
                f"{rc['nights']}, {int(rc['tp'] + rc['dv'])}, 0, {rc['late']})")
            cthd_idx += 1

    elif cfg['type'] == 'huy_som':
        hoa_don_rows.append(
            f"('{ma_hd}', '{ma_dat}', '{ma_nv}', '{fmt_dt(random_time(cfg['bd']))}', "
            f"0, 0, {int(total_coc)}, {VAT}, 0, 0, N'HOA_DON_HOAN_TIEN', N'DA_HOAN_COC', "
            f"N'Hoan coc do huy som', 0, 0)")
    else:
        hoa_don_rows.append(
            f"('{ma_hd}', '{ma_dat}', '{ma_nv}', '{fmt_dt(random_time(cfg['bd'],(10,18)))}', "
            f"0, 0, {int(total_coc)}, {VAT}, 0, 0, N'HOA_DON_PHONG', N'DA_MAT_COC', "
            f"N'Mat coc do huy muon', 0, 0)")

    hd_idx += 1; dp_idx += 1

# ===== OUTPUT =====
out = []
out.append("-- [BANG DATPHONG]")
out.append("INSERT INTO DatPhong (maDat, ngayDat, maKH, ngayCheckIn, ngayCheckOut, trangThai) VALUES")
for i, r in enumerate(dat_phong_rows): out.append(r + (',' if i < len(dat_phong_rows)-1 else ';'))
out.append("")
out.append("-- [BANG CHITIETDATPHONG]")
out.append("INSERT INTO ChiTietDatPhong (maCTDP, maPhong, maDat, giaCoc, soNguoi) VALUES")
for i, r in enumerate(ctdp_rows): out.append(r + (',' if i < len(ctdp_rows)-1 else ';'))
out.append("")
out.append("-- [BANG DICHVUSUDUNG]")
out.append("INSERT INTO DichVuSuDung (maDV, maCTDP, ngaySuDung, soLuong, giaDV, trangThai) VALUES")
for i, r in enumerate(dvsd_rows): out.append(r + (',' if i < len(dvsd_rows)-1 else ';'))
out.append("")
out.append("-- [BANG HOADON]")
out.append("INSERT INTO HoaDon (maHD, maDat, maNV, ngayTaoHD, tienPhong, tienDV, tienCoc, thueVAT, tongTien, doanhThu, loaiHD, trangThaiThanhToan, ghiChuThanhToan, phu_thu, phu_phi_tra_muon) VALUES")
for i, r in enumerate(hoa_don_rows): out.append(r + (',' if i < len(hoa_don_rows)-1 else ';'))
out.append("")
out.append("-- [BANG CHITIETHOADON]")
out.append("INSERT INTO ChiTietHoaDon (maCTHD, maHD, maCTDP, thoiGianLuuTru, thanhTien, phu_thu, phu_phi_tra_muon) VALUES")
for i, r in enumerate(cthd_rows): out.append(r + (',' if i < len(cthd_rows)-1 else ';'))

path = r"c:\Users\ACER\Desktop\Lucia_Hotel\sql\Lucia_DuLieu_Past.sql"
with open(path, 'w', encoding='utf-8') as f: f.write("\n".join(out))

late_ct = sum(1 for r in hoa_don_rows if "Tra muon" in r)
print(f"Bookings: {len(dat_phong_rows)}, Rooms: {len(ctdp_rows)}, Invoices: {len(hoa_don_rows)}, CTHD: {len(cthd_rows)}")
print(f"Late checkouts: {late_ct}")
print(f"Written to {path}")
