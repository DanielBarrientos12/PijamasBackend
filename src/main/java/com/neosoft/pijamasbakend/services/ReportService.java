package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.models.VentasPorMesDto;
import com.neosoft.pijamasbakend.repositories.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {
    @Autowired
    private FacturaRepository facturaRepo;

    public List<VentasPorMesDto> obtenerVentasMensuales() {
        return facturaRepo.findVentasMensualesPagadas();
    }
}
