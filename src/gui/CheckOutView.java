package gui;

import dao.DatPhongDAO;
import dao.DichVuSuDungDAO;
import dao.HoaDonDAO;
import dao.ChiTietHoaDonDAO;
import dao.PhongDAO;
import dao.ChiTietDatPhongDAO;
import model.entities.DatPhong;
import model.entities.DichVuSuDung;
import model.entities.HoaDon;
import model.entities.NhanVien;
import model.utils.InvoiceExporter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CheckOutView extends BorderPane {
    private static final String C_BG="#f8f9fa",C_BORDER="#e9ecef",C_TEXT_DARK="#111827",C_TEXT_GRAY="#6b7280";
    private static final String C_NAVY="#1e3a8a",C_BLUE="#1d4ed8",C_BLUE_HOVER="#1e40af",C_GREEN="#16a34a";
    private static final String C_RED="#dc2626";
    private static final double VAT_RATE=0.1;

    private final DatPhongDAO datPhongDAO=new DatPhongDAO();
    private final DichVuSuDungDAO dvsdDAO=new DichVuSuDungDAO();
    private final HoaDonDAO hoaDonDAO=new HoaDonDAO();
    private final ChiTietHoaDonDAO cthdDAO=new ChiTietHoaDonDAO();
    private final PhongDAO phongDAO=new PhongDAO();
    private final ChiTietDatPhongDAO ctdpDAO=new ChiTietDatPhongDAO();

    private TextField txtSearch, txtLateFee;
    private TableView<Object[]> tableOrders;
    private ObservableList<Object[]> orderData;
    private TableView<DichVuSuDung> serviceTable;
    private VBox detailSection, roomListSection, billingSection;
    private Label lblTotalDV, lblVAT, lblTongTien;
    private Button btnConfirm;
    private boolean isLateCheckout=false;

    private DatPhong currentDatPhong;
    private List<Object[]> currentRoomList;
    private double currentTienPhong=0,currentTienDV=0,currentTienCoc=0,currentLateFee=0,currentTongTien=0;
    private long currentSoDem=0;
    private NhanVien staff;

    public CheckOutView(){this(null);}
    public CheckOutView(NhanVien staff){
        this.staff=staff;
        setStyle("-fx-background-color:"+C_BG+";");
        setPadding(new Insets(32));
        setTop(buildHeader());
        setCenter(buildMainContent());
        resetView();
        refreshTable();
    }

    private boolean isModeDon=true; // true=theo đơn, false=theo phòng
    private ToggleButton btnModeDon,btnModePhong;

    private VBox buildHeader(){
        VBox header=new VBox(16);
        header.setPadding(new Insets(0,0,20,0));
        VBox titleBox=new VBox(4);
        Label t=new Label("Thủ tục trả phòng");
        t.setFont(Font.font("Segoe UI",FontWeight.BOLD,28));t.setTextFill(Color.web(C_TEXT_DARK));
        Label s=new Label("Tìm đơn đặt phòng theo mã đặt, số điện thoại hoặc CCCD để thực hiện trả phòng");
        s.setFont(Font.font("Segoe UI",14));s.setTextFill(Color.web(C_TEXT_GRAY));
        titleBox.getChildren().addAll(t,s);

        // Mode toggle
        HBox modeRow=new HBox(0);modeRow.setAlignment(Pos.CENTER_LEFT);
        String modeActive="-fx-background-color:"+C_BLUE+";-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:8 20;-fx-background-radius:8 0 0 8;-fx-border-color:"+C_BLUE+";-fx-border-radius:8 0 0 8;";
        String modeInactive="-fx-background-color:white;-fx-text-fill:"+C_TEXT_GRAY+";-fx-font-weight:bold;-fx-padding:8 20;-fx-background-radius:0 8 8 0;-fx-border-color:"+C_BORDER+";-fx-border-radius:0 8 8 0;";
        btnModeDon=new ToggleButton("📋 Trả phòng theo đơn đặt");btnModeDon.setCursor(Cursor.HAND);btnModeDon.setPrefHeight(38);
        btnModePhong=new ToggleButton("🚪 Trả phòng theo phòng");btnModePhong.setCursor(Cursor.HAND);btnModePhong.setPrefHeight(38);
        ToggleGroup modeGroup=new ToggleGroup();btnModeDon.setToggleGroup(modeGroup);btnModePhong.setToggleGroup(modeGroup);
        btnModeDon.setSelected(true);btnModeDon.setStyle(modeActive);btnModePhong.setStyle(modeInactive);
        modeGroup.selectedToggleProperty().addListener((obs,o,n)->{
            if(n==null){o.setSelected(true);return;}
            isModeDon=n==btnModeDon;
            btnModeDon.setStyle(isModeDon?modeActive:modeInactive.replace("0 8 8 0","8 0 0 8"));
            btnModePhong.setStyle(!isModeDon?modeActive.replace("8 0 0 8","0 8 8 0"):modeInactive);
            txtSearch.setPromptText(isModeDon?"Nhập mã đặt phòng, số điện thoại hoặc CCCD":"Nhập mã phòng (VD: P205)");
            resetView();refreshTable();
        });
        modeRow.getChildren().addAll(btnModeDon,btnModePhong);

        HBox searchRow=new HBox(12);searchRow.setAlignment(Pos.CENTER_LEFT);
        txtSearch=new TextField();
        txtSearch.setPromptText("Nhập mã đặt phòng, số điện thoại hoặc CCCD");
        txtSearch.setPrefHeight(40);HBox.setHgrow(txtSearch,Priority.ALWAYS);
        txtSearch.setStyle("-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:"+C_BORDER+";-fx-font-size:14px;-fx-padding:0 16;");
        txtSearch.setOnAction(e->handleSearch());
        Button btnS=new Button("\uD83D\uDD0D  Tìm kiếm");btnS.setPrefHeight(35);btnS.setMinWidth(140);btnS.setCursor(Cursor.HAND);
        styleButton(btnS,C_BLUE,"white",C_BLUE_HOVER);btnS.setOnAction(e->handleSearch());
        searchRow.getChildren().addAll(txtSearch,btnS);
        header.getChildren().addAll(titleBox,modeRow,searchRow);
        return header;
    }

    private HBox buildMainContent(){
        HBox main=new HBox(24);main.setAlignment(Pos.TOP_LEFT);

        // LEFT COLUMN
        VBox leftCol=new VBox(16);leftCol.setMinWidth(600);leftCol.setPrefWidth(640);HBox.setHgrow(leftCol,Priority.ALWAYS);
        VBox combinedWrapper=new VBox(16);
        combinedWrapper.setPadding(new Insets(24));
        combinedWrapper.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:"+C_BORDER+";-fx-border-radius:12;");
        combinedWrapper.setEffect(new DropShadow(10,0,4,Color.web("#00000008")));

        detailSection=new VBox(16);detailSection.setAlignment(Pos.CENTER);
        Label noData=new Label("Chưa có thông tin đơn đặt phòng");noData.setTextFill(Color.web(C_TEXT_GRAY));
        detailSection.getChildren().add(noData);

        Separator sep1=new Separator();sep1.setPadding(new Insets(8,0,8,0));
        Label lblRoom=new Label("Phòng trong đơn");
        lblRoom.setFont(Font.font("Segoe UI",FontWeight.BOLD,16));lblRoom.setTextFill(Color.web(C_TEXT_DARK));
        roomListSection=new VBox(8);
        Label noRoom=new Label("Chọn đơn để xem danh sách phòng");noRoom.setTextFill(Color.web(C_TEXT_GRAY));
        roomListSection.getChildren().add(noRoom);
        ScrollPane scrollRooms=new ScrollPane(roomListSection);scrollRooms.setFitToWidth(true);scrollRooms.setPrefHeight(160);
        scrollRooms.setStyle("-fx-background:white;-fx-background-color:white;-fx-border-color:#f3f4f6;-fx-border-radius:8;");

        // Service table
        Separator sep2=new Separator();sep2.setPadding(new Insets(8,0,8,0));
        Label lblSvc=new Label("Dịch vụ đã sử dụng");
        lblSvc.setFont(Font.font("Segoe UI",FontWeight.BOLD,16));lblSvc.setTextFill(Color.web(C_TEXT_DARK));
        serviceTable=new TableView<>();
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        serviceTable.setPlaceholder(new Label("Không có dữ liệu"));
        TableColumn<DichVuSuDung,String> colNgay=new TableColumn<>("Ngày");
        colNgay.setCellValueFactory(p->new SimpleStringProperty(p.getValue().getNgaySuDung()!=null?p.getValue().getNgaySuDung().toString():""));
        TableColumn<DichVuSuDung,String> colTen=new TableColumn<>("Dịch vụ");
        colTen.setCellValueFactory(p->new SimpleStringProperty(p.getValue().getDichVu()!=null?p.getValue().getDichVu().getTenDV():""));
        TableColumn<DichVuSuDung,String> colDonVi=new TableColumn<>("Đơn vị");
        colDonVi.setCellValueFactory(p->new SimpleStringProperty(p.getValue().getDichVu()!=null&&p.getValue().getDichVu().getDonVi()!=null?p.getValue().getDichVu().getDonVi():""));
        colDonVi.setMaxWidth(80);colDonVi.setStyle("-fx-alignment:CENTER;");
        TableColumn<DichVuSuDung,String> colSl=new TableColumn<>("SL");
        colSl.setCellValueFactory(p->new SimpleStringProperty(String.valueOf(p.getValue().getSoLuong())));
        colSl.setMaxWidth(50);colSl.setStyle("-fx-alignment:CENTER;");
        TableColumn<DichVuSuDung,String> colGia=new TableColumn<>("Đơn giá");
        colGia.setCellValueFactory(p->new SimpleStringProperty(String.format("%,.0f đ",p.getValue().getGiaDV())));
        colGia.setStyle("-fx-alignment:CENTER-RIGHT;");
        TableColumn<DichVuSuDung,String> colTT=new TableColumn<>("Thành tiền");
        colTT.setCellValueFactory(p->new SimpleStringProperty(String.format("%,.0f đ",p.getValue().getThanhTien())));
        colTT.setStyle("-fx-alignment:CENTER-RIGHT;-fx-font-weight:bold;");
        serviceTable.getColumns().addAll(colNgay,colTen,colDonVi,colSl,colGia,colTT);
        for(TableColumn<DichVuSuDung,?>c:serviceTable.getColumns()){c.setReorderable(false);c.setSortable(false);}

        lblTotalDV=new Label("Tổng tiền dịch vụ: 0 đ");
        lblTotalDV.setFont(Font.font("Segoe UI",FontWeight.BOLD,14));
        lblTotalDV.setTextFill(Color.web(C_BLUE));
        lblTotalDV.setAlignment(Pos.CENTER_RIGHT);lblTotalDV.setMaxWidth(Double.MAX_VALUE);
        lblTotalDV.setStyle("-fx-padding:4 8;-fx-background-color:#f0f9ff;-fx-background-radius:6;");

        // Billing inline
        Separator sep3=new Separator();sep3.setPadding(new Insets(8,0,8,0));
        billingSection=new VBox(12);

        // Action buttons
        HBox actionRow=new HBox(12);actionRow.setAlignment(Pos.CENTER_RIGHT);actionRow.setPadding(new Insets(12,0,0,0));
        Button btnCancel=new Button("Hủy bỏ");btnCancel.setPrefHeight(40);btnCancel.setMinWidth(90);btnCancel.setCursor(Cursor.HAND);
        btnCancel.setStyle("-fx-background-color:#f3f4f6;-fx-text-fill:#4b5563;-fx-background-radius:8;-fx-font-weight:bold;");
        btnCancel.setOnAction(e->{resetView();refreshTable();});
        btnConfirm=new Button("💳  Xác nhận thanh toán");btnConfirm.setPrefHeight(40);btnConfirm.setMinWidth(220);btnConfirm.setCursor(Cursor.HAND);
        styleButton(btnConfirm,"#1e3a8aEE","white","#1e3a8aEE");
        btnConfirm.setVisible(false);btnConfirm.setManaged(false);
        btnConfirm.setOnAction(e->handleCheckOut());
        actionRow.getChildren().addAll(btnCancel,btnConfirm);

        combinedWrapper.getChildren().addAll(detailSection,sep1,lblRoom,scrollRooms,sep2,lblSvc,serviceTable,lblTotalDV,sep3,billingSection,actionRow);
        ScrollPane leftScroll=new ScrollPane(combinedWrapper);leftScroll.setFitToWidth(true);
        leftScroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");
        VBox.setVgrow(leftScroll,Priority.ALWAYS);
        leftCol.getChildren().add(leftScroll);

        // RIGHT COLUMN - order list
        VBox rightCol=new VBox(16);HBox.setHgrow(rightCol,Priority.ALWAYS);
        VBox orderContainer=new VBox(16);orderContainer.setPadding(new Insets(24));
        orderContainer.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:"+C_BORDER+";-fx-border-radius:12;");
        orderContainer.setEffect(new DropShadow(10,0,4,Color.web("#00000008")));
        VBox.setVgrow(orderContainer,Priority.ALWAYS);
        Label lblOT=new Label("Danh sách đơn trả phòng");
        lblOT.setFont(Font.font("Segoe UI",FontWeight.BOLD,18));lblOT.setTextFill(Color.web(C_TEXT_DARK));



        // Table – giới hạn chiều cao để dành chỗ cho DV bên trái
        orderData=FXCollections.observableArrayList();
        tableOrders=new TableView<>(orderData);tableOrders.setPlaceholder(new Label("Không có đơn trả phòng nào"));
        tableOrders.setStyle("-fx-font-size:13px;");VBox.setVgrow(tableOrders,Priority.ALWAYS);
        TableColumn<Object[],String> c1=new TableColumn<>("Mã đặt");c1.setCellValueFactory(p->new SimpleStringProperty((String)p.getValue()[0]));c1.setMinWidth(80);
        TableColumn<Object[],String> c2=new TableColumn<>("Tên khách hàng");c2.setCellValueFactory(p->new SimpleStringProperty((String)p.getValue()[1]));c2.setMinWidth(130);
        TableColumn<Object[],String> c3=new TableColumn<>("SĐT");c3.setCellValueFactory(p->new SimpleStringProperty((String)p.getValue()[2]));c3.setMinWidth(100);
        TableColumn<Object[],String> c4=new TableColumn<>("Phòng");c4.setCellValueFactory(p->new SimpleStringProperty((String)p.getValue()[3]));c4.setMinWidth(100);
        TableColumn<Object[],String> c5=new TableColumn<>("Số phòng");c5.setCellValueFactory(p->new SimpleStringProperty(String.valueOf(p.getValue()[4])));c5.setStyle("-fx-alignment:CENTER;");c5.setMinWidth(60);c5.setMaxWidth(80);
        tableOrders.getColumns().addAll(c1,c2,c3,c4,c5);
        tableOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for(TableColumn<Object[],?>c:tableOrders.getColumns()){c.setReorderable(false);c.setSortable(false);}
        tableOrders.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->{
            if(n!=null){
                // In room mode, search by maPhong (col 3); in don mode, search by maDat (col 0)
                String searchKey=isModeDon?(String)n[0]:(String)n[3];
                txtSearch.setText(searchKey);
                handleSearch();
            }
        });
        orderContainer.getChildren().addAll(lblOT,tableOrders);
        rightCol.getChildren().add(orderContainer);
        main.getChildren().addAll(leftCol,rightCol);
        return main;
    }
    // === PART 2: LOGIC ===

    private void refreshTable(){
        orderData.clear();
        if(isModeDon){
            // Mode: Trả phòng theo đơn đặt
            List<String> dsDon=datPhongDAO.getDonDangSuDung();
            for(String maDat:dsDon){
                DatPhong dp=datPhongDAO.findDatPhongForCheckOut(maDat);
                if(dp!=null){
                    String tenKH=dp.getKhachHang()!=null?dp.getKhachHang().getTenKH():"---";
                    String sdt=dp.getKhachHang()!=null&&dp.getKhachHang().getSoDT()!=null?dp.getKhachHang().getSoDT():"---";
                    Object[] info=datPhongDAO.findCheckOutInfoByMaDat(maDat);
                    String dsPhong="---";int soPhong=0;
                    if(info!=null){
                        @SuppressWarnings("unchecked") List<Object[]> rooms=(List<Object[]>)info[1];
                        StringBuilder sb=new StringBuilder();
                        for(Object[] r:rooms){if(sb.length()>0)sb.append(", ");sb.append((String)r[1]);}
                        dsPhong=sb.toString();soPhong=rooms.size();
                    }
                    orderData.add(new Object[]{maDat,tenKH,sdt,dsPhong,soPhong});
                }
            }
        } else {
            // Mode: Trả phòng theo phòng
            List<String> dsPhong=datPhongDAO.getPhongDangSuDung();
            for(String maPhong:dsPhong){
                Object[] info=datPhongDAO.findCheckOutInfoByMaPhong(maPhong);
                if(info==null)continue;
                DatPhong dp=(DatPhong)info[0];
                String tenKH=dp.getKhachHang()!=null?dp.getKhachHang().getTenKH():"---";
                String sdt=dp.getKhachHang()!=null&&dp.getKhachHang().getSoDT()!=null?dp.getKhachHang().getSoDT():"---";
                String maDat=dp.getMaDat();
                orderData.add(new Object[]{maDat,tenKH,sdt,maPhong,1});
            }
        }
    }

    private void handleSearch(){
        String key=txtSearch.getText().trim();if(key.isEmpty())return;
        String searchRoom=null; // track searched room for room mode
        DatPhong dp=datPhongDAO.findDatPhongForCheckOut(key);
        if(dp==null){
            // Try search by room
            Object[] info=datPhongDAO.findCheckOutInfoByMaPhong(key.toUpperCase());
            if(info!=null){
                dp=(DatPhong)info[0];
                searchRoom=key.toUpperCase();
            } else {
                new Alert(Alert.AlertType.WARNING,"Không tìm thấy đơn đặt phòng đang sử dụng.\nKiểm tra lại mã đặt / SĐT / CCCD / mã phòng.",ButtonType.OK).showAndWait();
                return;
            }
        }
        // Load full checkout info by maDat
        Object[] result=datPhongDAO.findCheckOutInfoByMaDat(dp.getMaDat());
        if(result==null){new Alert(Alert.AlertType.WARNING,"Không tìm thấy thông tin checkout.",ButtonType.OK).showAndWait();return;}
        currentDatPhong=(DatPhong)result[0];
        @SuppressWarnings("unchecked") List<Object[]> rooms=(List<Object[]>)result[1];

        // In room mode, only keep the specific room
        if(!isModeDon && searchRoom!=null){
            String finalSearchRoom=searchRoom;
            rooms=rooms.stream().filter(r->finalSearchRoom.equals((String)r[1])).collect(java.util.stream.Collectors.toList());
        }

        currentRoomList=rooms;
        if(currentRoomList.isEmpty()){new Alert(Alert.AlertType.INFORMATION,"Đơn này không có phòng nào đang sử dụng.",ButtonType.OK).showAndWait();return;}
        updateDetailUI();
        updateRoomListUI();
        loadAllServices();
        calculateBilling();
        updateBillingUI();
        btnConfirm.setVisible(true);btnConfirm.setManaged(true);
    }

    private void updateDetailUI(){
        detailSection.getChildren().clear();detailSection.setAlignment(Pos.TOP_LEFT);
        DateTimeFormatter dtf=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        VBox infoBox=new VBox(12);
        infoBox.getChildren().addAll(
            createDetailItem("Mã đơn",currentDatPhong.getMaDat()),
            createDetailItem("Khách hàng",currentDatPhong.getKhachHang().getTenKH()),
            createDetailItem("Số điện thoại",safe(currentDatPhong.getKhachHang().getSoDT())),
            createDetailItem("Số CCCD",safe(currentDatPhong.getKhachHang().getSoCCCD())));
        Separator sep=new Separator();sep.setPadding(new Insets(8,0,8,0));
        String trangThaiStr=safe(currentDatPhong.getTrangThai());
        try{trangThaiStr=model.enums.TrangThaiDatPhong.valueOf(currentDatPhong.getTrangThai()).getThongTinTrangThai();}catch(Exception ignored){}
        VBox scheduleBox=new VBox(12);
        scheduleBox.getChildren().addAll(
            createDetailItem("Ngày nhận",currentDatPhong.getNgayCheckIn()!=null?currentDatPhong.getNgayCheckIn().format(dtf):"---"),
            createDetailItem("Ngày trả dự kiến",currentDatPhong.getNgayCheckOut()!=null?currentDatPhong.getNgayCheckOut().format(dtf):"---"),
            createDetailItem("Trạng thái đơn",trangThaiStr));
        detailSection.getChildren().addAll(infoBox,sep,scheduleBox);
    }

    private void updateRoomListUI(){
        roomListSection.getChildren().clear();
        if(currentRoomList==null||currentRoomList.isEmpty()){
            Label lbl=new Label("Đơn này chưa có phòng nào đang sử dụng");lbl.setTextFill(Color.web("#d97706"));lbl.setWrapText(true);
            roomListSection.getChildren().add(lbl);return;
        }
        for(Object[] rowData:currentRoomList){
            String maPhong=(String)rowData[1];double giaPhong=(double)rowData[3];
            HBox row=new HBox(16);row.setAlignment(Pos.CENTER_LEFT);row.setPadding(new Insets(10,14,10,14));
            row.setStyle("-fx-background-color:#f0f9ff;-fx-background-radius:10;-fx-border-color:"+C_BLUE+";-fx-border-radius:10;-fx-border-width:1.5;");
            VBox roomInfo=new VBox(2);
            Label lblR=new Label("Phòng  "+maPhong);lblR.setFont(Font.font("Segoe UI",FontWeight.BOLD,14));lblR.setTextFill(Color.web(C_NAVY));
            Label lblPrice=new Label(String.format("%,.0f đ/đêm",giaPhong));lblPrice.setFont(Font.font("Segoe UI",12));lblPrice.setTextFill(Color.web(C_TEXT_GRAY));
            roomInfo.getChildren().addAll(lblR,lblPrice);
            Label lblStatus=new Label("Đang sử dụng");lblStatus.setFont(Font.font("Segoe UI",12));
            lblStatus.setTextFill(Color.web("#d97706"));lblStatus.setStyle("-fx-background-color:#fef3c7;-fx-padding:2 10;-fx-background-radius:10;");
            Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);
            row.getChildren().addAll(roomInfo,spacer,lblStatus);
            roomListSection.getChildren().add(row);
        }
    }

    private void loadAllServices(){
        java.util.List<DichVuSuDung> allDV=new java.util.ArrayList<>();
        for(Object[] room:currentRoomList){
            String maCTDP=(String)room[0];
            List<DichVuSuDung> dvRoom=dvsdDAO.findByMaCTDP(maCTDP);
            if(dvRoom!=null)allDV.addAll(dvRoom);
        }
        serviceTable.setItems(FXCollections.observableArrayList(allDV));
        currentTienDV=allDV.stream().mapToDouble(DichVuSuDung::getThanhTien).sum();
        lblTotalDV.setText(String.format("Tổng tiền dịch vụ: %,.0f đ",currentTienDV));
    }

    private void calculateBilling(){
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime checkIn=currentDatPhong.getNgayCheckIn();
        currentSoDem=checkIn!=null?Math.max(1,Duration.between(checkIn,now).toDays()):1;
        double lateFeeRate=0;
        isLateCheckout=false;
        if(currentDatPhong.getNgayCheckOut()!=null&&now.isAfter(currentDatPhong.getNgayCheckOut())){
            isLateCheckout=true;
            int hour=now.getHour();
            if(hour>=18)lateFeeRate=1;else if(hour>=15)lateFeeRate=0.5;else if(hour>=12)lateFeeRate=0.3;
        }
        currentTienPhong=0;currentTienCoc=0;currentLateFee=0;
        for(Object[] room:currentRoomList){
            double giaCoc=(double)room[2];double giaPhong=(double)room[3];
            currentTienPhong+=giaPhong*currentSoDem;
            currentTienCoc+=giaCoc;
            currentLateFee+=giaPhong*lateFeeRate;
        }
        double base=currentTienPhong+currentLateFee+currentTienDV;
        currentTongTien=Math.max(0,base*(1+VAT_RATE)-currentTienCoc);
    }

    private void updateBillingUI(){
        billingSection.getChildren().clear();
        Label lblT=new Label("Chi tiết hóa đơn");lblT.setFont(Font.font("Segoe UI",FontWeight.BOLD,18));lblT.setTextFill(Color.web(C_TEXT_DARK));

        // Editable late fee
        HBox lateFeeRow=new HBox(8);lateFeeRow.setAlignment(Pos.CENTER_LEFT);
        Label lblLF=new Label("Phụ phí trả muộn:");lblLF.setTextFill(Color.web(C_TEXT_GRAY));lblLF.setFont(Font.font("Segoe UI",13));
        lblLF.setMaxWidth(Double.MAX_VALUE);HBox.setHgrow(lblLF,Priority.ALWAYS);
        txtLateFee=new TextField(String.format("%.0f",currentLateFee));
        txtLateFee.setPrefWidth(120);
        // Numeric-only filter
        txtLateFee.setTextFormatter(new TextFormatter<>(change->{
            String newText=change.getControlNewText();
            if(newText.isEmpty()||newText.matches("[0-9]+"))return change;
            return null;
        }));
        // Disable if not late
        if(!isLateCheckout){
            txtLateFee.setDisable(true);
            txtLateFee.setStyle("-fx-alignment:CENTER-RIGHT;-fx-font-weight:bold;-fx-border-color:"+C_BORDER+";-fx-border-radius:6;-fx-background-radius:6;-fx-opacity:0.5;");
        } else {
            txtLateFee.setDisable(false);
            txtLateFee.setStyle("-fx-alignment:CENTER-RIGHT;-fx-font-weight:bold;-fx-border-color:"+C_BORDER+";-fx-border-radius:6;-fx-background-radius:6;");
        }
        txtLateFee.textProperty().addListener((obs,o,n)->{
            try{currentLateFee=n.isEmpty()?0:Double.parseDouble(n);recalcTotal();}catch(NumberFormatException ignored){}
        });
        Label lblDong=new Label("đ");lblDong.setFont(Font.font("Segoe UI",FontWeight.BOLD,13));
        lateFeeRow.getChildren().addAll(lblLF,txtLateFee,lblDong);

        // VAT label (instance field for live update)
        lblVAT=new Label(String.format("%,.0f đ",(currentTienPhong+currentLateFee+currentTienDV)*VAT_RATE));
        lblVAT.setMinWidth(Region.USE_PREF_SIZE);lblVAT.setFont(Font.font("Segoe UI",FontWeight.BOLD,14));lblVAT.setTextFill(Color.web(C_TEXT_DARK));

        VBox rows=new VBox(12);
        rows.getChildren().addAll(
            createBillRow("Tiền phòng ("+currentSoDem+" đêm)",currentTienPhong,Color.web(C_TEXT_DARK)),
            createBillRow("Tiền dịch vụ",currentTienDV,Color.web(C_TEXT_DARK)),
            lateFeeRow,
            createBillRow("Tiền cọc (đã khấu trừ)",-currentTienCoc,Color.web(C_GREEN)));
        // VAT row with live label
        HBox vatRow=new HBox();vatRow.setMaxWidth(Double.MAX_VALUE);vatRow.setAlignment(Pos.CENTER_LEFT);
        Label vatLbl=new Label(String.format("Thuế VAT (%.0f%%)",VAT_RATE*100));vatLbl.setTextFill(Color.web(C_TEXT_GRAY));vatLbl.setFont(Font.font("Segoe UI",13));
        vatLbl.setMaxWidth(Double.MAX_VALUE);HBox.setHgrow(vatLbl,Priority.ALWAYS);
        vatRow.getChildren().addAll(vatLbl,lblVAT);
        rows.getChildren().add(vatRow);

        HBox totalRow=new HBox();totalRow.setMaxWidth(Double.MAX_VALUE);totalRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTotal=new Label("TỔNG THANH TOÁN");lblTotal.setFont(Font.font("Segoe UI",FontWeight.BOLD,16));
        lblTotal.setMaxWidth(Double.MAX_VALUE);HBox.setHgrow(lblTotal,Priority.ALWAYS);
        lblTongTien=new Label(String.format("%,.0f đ",currentTongTien));lblTongTien.setMinWidth(Region.USE_PREF_SIZE);
        lblTongTien.setFont(Font.font("Segoe UI",FontWeight.BOLD,24));lblTongTien.setTextFill(Color.web(C_BLUE));
        totalRow.getChildren().addAll(lblTotal,lblTongTien);
        billingSection.getChildren().addAll(lblT,new Separator(),rows,new Separator(),totalRow);
    }

    private void recalcTotal(){
        double base=currentTienPhong+currentLateFee+currentTienDV;
        currentTongTien=Math.max(0,base*(1+VAT_RATE)-currentTienCoc);
        // Update labels directly — no rebuild, no focus loss
        if(lblVAT!=null)lblVAT.setText(String.format("%,.0f đ",(currentTienPhong+currentLateFee+currentTienDV)*VAT_RATE));
        if(lblTongTien!=null)lblTongTien.setText(String.format("%,.0f đ",currentTongTien));
    }

    private void handleCheckOut(){
        if(currentDatPhong==null||currentRoomList==null)return;
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION);confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Checkout "+currentRoomList.size()+" phòng cho khách "+currentDatPhong.getKhachHang().getTenKH()+"?");
        confirm.setContentText("Tổng: "+String.format("%,.0f đ",currentTongTien));
        confirm.showAndWait().ifPresent(type->{
            if(type==ButtonType.OK){
                HoaDon hd=performCheckOut();
                if(hd!=null){
                    HoaDonView.showHoaDonDetail(hd);
                    resetView();refreshTable();serviceTable.getItems().clear();
                } else {
                    new Alert(Alert.AlertType.ERROR,"Lưu dữ liệu thất bại. Vui lòng thử lại.").showAndWait();
                }
            }
        });
    }

    private HoaDon performCheckOut(){
        try{
            LocalDateTime now=LocalDateTime.now();
            HoaDon hd=hoaDonDAO.getByMaDat(currentDatPhong.getMaDat());
            if(hd==null){
                hd=new HoaDon();hd.setMaHD(hoaDonDAO.generateMaHD());
                hd.setDatPhong(currentDatPhong);
                hd.setNhanVien(staff!=null?staff:new NhanVien("LUCIA001"));
                hd.setNgayTaoHD(now);hd.setTienPhong(0);hd.setTienDV(0);
                hd.setTienCoc(ctdpDAO.getTongCocByMaDat(currentDatPhong.getMaDat()));
                hd.setThueVAT(VAT_RATE);hoaDonDAO.tinhTongTien(hd);hoaDonDAO.tinhDoanhThu(hd);hoaDonDAO.insert(hd);
            }
            // Luôn gán DatPhong đầy đủ (có KhachHang) để popup hiển thị đúng
            hd.setDatPhong(currentDatPhong);
            for(Object[] room:currentRoomList){
                String maCTDP=(String)room[0];String maPhong=(String)room[1];double giaPhong=(double)room[3];
                double tienPhong=currentSoDem*giaPhong;
                String maCTHD=cthdDAO.getMaCTHDByMaCTDP(maCTDP);
                if(maCTHD!=null){cthdDAO.updateLuuTruVaTien(maCTHD,currentSoDem,tienPhong);}
                else{maCTHD=cthdDAO.generateMaCTHD();cthdDAO.insert(maCTHD,hd.getMaHD(),maCTDP,currentSoDem,tienPhong);}
                phongDAO.updateTrangThai(maPhong,"BAN");
            }
            double sumPhong=hoaDonDAO.getTongTienPhongCurrent(hd.getMaHD());
            List<DichVuSuDung> listDV=dvsdDAO.findByMaHD(hd.getMaHD());
            double tienDV=listDV.stream().mapToDouble(DichVuSuDung::getThanhTien).sum();
            hd.setTienPhong(sumPhong);hd.setTienDV(tienDV);hd.setThueVAT(VAT_RATE);hd.setNgayTaoHD(now);
            double newTong=Math.max(0,(sumPhong+tienDV)*(1+VAT_RATE)-hd.getTienCoc());
            hd.setTongTien(newTong);
            if(datPhongDAO.isAllRoomsCheckedOut(currentDatPhong.getMaDat())){
                datPhongDAO.updateTrangThai(currentDatPhong.getMaDat(),"DA_CHECKOUT");
                hd.setTrangThaiThanhToan("DA_THANH_TOAN");
            }
            if(hoaDonDAO.updateTongTien(hd))return hd;
            return null;
        }catch(Exception e){e.printStackTrace();return null;}
    }



    private void resetView(){
        if(txtSearch!=null)txtSearch.clear();
        currentDatPhong=null;currentRoomList=null;
        currentTienPhong=0;currentTienDV=0;currentTienCoc=0;currentLateFee=0;currentTongTien=0;currentSoDem=0;
        if(btnConfirm!=null){btnConfirm.setVisible(false);btnConfirm.setManaged(false);}
        if(detailSection!=null){detailSection.getChildren().clear();detailSection.setAlignment(Pos.CENTER);
            Label lbl=new Label("Chưa có thông tin đơn đặt phòng");lbl.setTextFill(Color.web(C_TEXT_GRAY));detailSection.getChildren().add(lbl);}
        if(roomListSection!=null){roomListSection.getChildren().clear();
            Label lbl=new Label("Chọn đơn để xem danh sách phòng");lbl.setTextFill(Color.web(C_TEXT_GRAY));roomListSection.getChildren().add(lbl);}
        if(serviceTable!=null)serviceTable.setItems(FXCollections.observableArrayList());
        if(lblTotalDV!=null)lblTotalDV.setText("Tổng tiền dịch vụ: 0 đ");
        if(billingSection!=null)billingSection.getChildren().clear();
        if(tableOrders!=null)tableOrders.getSelectionModel().clearSelection();
    }

    // === HELPERS ===
    private String safe(String s){return s!=null?s:"---";}
    private HBox createDetailItem(String label,String value){
        HBox hb=new HBox();hb.setAlignment(Pos.CENTER_LEFT);
        Label lbl=new Label(label+":");lbl.setMinWidth(160);lbl.setTextFill(Color.web(C_TEXT_GRAY));lbl.setFont(Font.font("Segoe UI",14));
        Label val=new Label(value!=null?value:"---");val.setFont(Font.font("Segoe UI",FontWeight.BOLD,14));val.setTextFill(Color.web(C_TEXT_DARK));
        hb.getChildren().addAll(lbl,val);return hb;
    }
    private HBox createBillRow(String label,double value,Color valColor){
        HBox hb=new HBox();hb.setMaxWidth(Double.MAX_VALUE);hb.setAlignment(Pos.CENTER_LEFT);
        Label lbl=new Label(label);lbl.setTextFill(Color.web(C_TEXT_GRAY));lbl.setFont(Font.font("Segoe UI",13));
        lbl.setMaxWidth(Double.MAX_VALUE);HBox.setHgrow(lbl,Priority.ALWAYS);
        Label val=new Label(String.format("%,.0f đ",value));val.setMinWidth(Region.USE_PREF_SIZE);
        val.setFont(Font.font("Segoe UI",FontWeight.BOLD,14));val.setTextFill(valColor);
        hb.getChildren().addAll(lbl,val);return hb;
    }

    private void styleButton(Button btn,String bg,String fg,String hoverBg){
        String base="-fx-background-color:"+bg+";-fx-text-fill:"+fg+";-fx-background-radius:8;-fx-padding:10 20;-fx-cursor:hand;-fx-font-weight:bold;";
        String hover=base.replace("-fx-background-color:"+bg,"-fx-background-color:"+hoverBg);
        btn.setStyle(base);btn.setOnMouseEntered(e->btn.setStyle(hover));btn.setOnMouseExited(e->btn.setStyle(base));
    }
}
