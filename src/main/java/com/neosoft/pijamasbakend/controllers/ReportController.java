package com.neosoft.pijamasbakend.controllers;

import com.neosoft.pijamasbakend.models.VentasPorMesDto;
import com.neosoft.pijamasbakend.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/ventas-mes")
    public ResponseEntity<List<VentasPorMesDto>> ventasPorMes() {
        List<VentasPorMesDto> data = reportService.obtenerVentasMensuales();
        return ResponseEntity.ok(data);
    }
}