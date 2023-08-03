package com.ChickenTest.demoChickenTest.controller;

import com.ChickenTest.demoChickenTest.GlobalExceptionHandler;
import com.ChickenTest.demoChickenTest.dto.FarmDashboardDto;
import com.ChickenTest.demoChickenTest.dto.FarmDto;
import com.ChickenTest.demoChickenTest.service.impl.FarmService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
public class FarmController {
    private static final Logger logger = Logger.getLogger(FarmController.class);
    @Autowired
    FarmService farmService;

    @GetMapping("/")
    public String getDashboardProperties(Model model){
        FarmDashboardDto farmDashboardDto = farmService.getPropertiesDashboard();
        model.addAttribute("farm", farmDashboardDto);

        logger.info("Farm properties: {}", farmDashboardDto);
        return "index";
    }

    @PostMapping("/buy/chicken/{cantidad}")
    public String buyChicken(Model model,@PathVariable int cantidad){
        FarmDashboardDto farmDashboardDto = farmService.getPropertiesDashboard();
        try {
            logger.info(farmService.getPropertiesDashboard());
            farmService.buy("chicken", cantidad);
            model.addAttribute("farm", farmDashboardDto);
        }catch (Exception e){
            logger.error("No se pudo realizar la compra: " + e.getMessage());
        }

        return "index";
    }

    @PostMapping("/buy/egg/{cantidad}")
    public String buyEgg(Model model,@PathVariable int cantidad){
        FarmDashboardDto farmDashboardDto = farmService.getPropertiesDashboard();
        farmService.buy("egg", cantidad);
        model.addAttribute("farm", farmDashboardDto);
        return "index";
    }

}
