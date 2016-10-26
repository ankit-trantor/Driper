package com.haui.object;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Duong on 10/26/2016.
 */
@IgnoreExtraProperties
public class TimXe {
    public Location location;
    public String viTri;
    public String maSV;
    public String diemDen;
    public String giaTien;
    public String diemDi;
    public String thongDiep;
    public TimXe() {
    }

    public Location getLocation() {
        return location;
    }

    public String getViTri() {
        return viTri;
    }

    public String getMaSV() {
        return maSV;
    }

    public String getDiemDen() {
        return diemDen;
    }

    public String getGiaTien() {
        return giaTien;
    }

    public String getDiemDi() {
        return diemDi;
    }

    public String getThongDiep() {
        return thongDiep;
    }

    public TimXe(Location location, String viTri, String maSV, String diemDen, String giaTien, String diemDi, String thongDiep) {

        this.location = location;
        this.viTri = viTri;
        this.maSV = maSV;
        this.diemDen = diemDen;
        this.giaTien = giaTien;
        this.diemDi = diemDi;
        this.thongDiep = thongDiep;
    }
}