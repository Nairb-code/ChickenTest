package com.ChickenTest.demoChickenTest.service.impl;

import com.ChickenTest.demoChickenTest.dto.*;
import com.ChickenTest.demoChickenTest.entity.*;
import com.ChickenTest.demoChickenTest.repository.IChickenRepository;
import com.ChickenTest.demoChickenTest.repository.IEggRepository;
import com.ChickenTest.demoChickenTest.repository.IFarmRepository;
import com.ChickenTest.demoChickenTest.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class FarmService {
    private static final Logger logger = Logger.getLogger(FarmService.class);
    @Autowired
    IFarmRepository farmRepository;
    @Autowired
    IChickenRepository chickenRepository;
    @Autowired
    IEggRepository eggRepository;
    @Autowired
    private ChickenService chickenService;
    @Autowired
    private EggService eggService;
    @Autowired
    ObjectMapper mapper;

    private Farm myFarm;
    private int countBreakEggs = 0;
    private int countChickenDeads = 0;

    private double chickenPriceToSell = Store.PRECIO_VENTA_CHICKEN;
    private double chickenPriceToBuy = Store.PRECIO_COMPRA_CHICKEN;
    private double eggPriceToSell = Store.PRECIO_VENTA_EGG;
    private double eggPriceToBuy = Store.PRECIO_COMPRA_EGG;

    public void save(Farm farm){
        farmRepository.save(farm);
    }
    public Farm getFarm(Long id){

        return farmRepository.findById(id).orElse(null);
    }

    public FarmTableDto getDataTableFarm(){
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow( ()-> new RuntimeException("Nose encontró ninguna granja registrada"));

        return mapper.convertValue(farm, FarmTableDto.class);
    }
    public SectionCashAvailable getCashAvailable(){
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow(()-> new RuntimeException("Nose encontró ninguna granja registrada"));

        SectionCashAvailable cashAvailable = mapper.convertValue(farm, SectionCashAvailable.class);

        cashAvailable.setSueldoBase(cashAvailable.getDinero() + farm.getGastos());

        return cashAvailable;
    }
    private int getDayPass(int cantidad){
        return LifeCycle.DAY_OF_LIFE_FARMER - cantidad;
    }
    private String getDayWithformatDate(int diasTranscurridos, String pattern){
        /*  1. Obtener la fecha actual.    */
        LocalDate currentDate = LocalDate.now();

        /*  2. Incrementar dia a la fecha actual. */
        LocalDate increasedDate = currentDate.plusDays(diasTranscurridos);

        /*  3. Crear un formateador de fecha con el formato deseado. */
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern, new Locale("es", "ES"));

        /*  4. Retornar la fecha con el formato definido  */

        return increasedDate.format(dateFormatter);
    }
    public ChickenStatusDto getCardChickenStatus(){
        ChickenStatusDto chickenStatusDto = new ChickenStatusDto();

        chickenStatusDto.setCountChickensDead(countChickenDeads);
        chickenStatusDto.setCountBreakEggs(countBreakEggs);

        return chickenStatusDto;
    }
    public SectionBuySellProducts getSectionBuySellProducts(){
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow( ()-> new RuntimeException("No hay ninguna granja registrada."));
        SectionBuySellProducts sectionBuySellProducts = mapper.convertValue(farm, SectionBuySellProducts.class);

        double precioTotalHuevosComprados = eggService.precioTotalComprado;//arm.getCantHuevos() * Store.PRECIO_COMPRA_EGG;
        double precioTotalPollosComprados = chickenService.precioTotalComprado; //farm.getCantPollos() * Store.PRECIO_COMPRA_CHICKEN;

        double precioTotalHuevosVendidos = eggService.precioTotalVendido;
        double precioTotalPollosVendidos = chickenService.precioTotalVendido;//farm.getCantPollosVendidos() * Store.PRECIO_VENTA_CHICKEN;

        sectionBuySellProducts.setPrecioTotalHuevosComprados(precioTotalHuevosComprados);
        sectionBuySellProducts.setPrecioTotalPollosComprados(precioTotalPollosComprados);
        sectionBuySellProducts.setCantPollos(chickenService.cantidadComprados);
        sectionBuySellProducts.setCantHuevos(eggService.cantidadComprados);

        sectionBuySellProducts.setPrecioTotalHuevosVendidos(precioTotalHuevosVendidos);
        sectionBuySellProducts.setPrecioTotalPollosVendidos(precioTotalPollosVendidos);
        sectionBuySellProducts.setCantPollosVendidos(chickenService.cantidadVendidos);
        sectionBuySellProducts.setCantHuevosVendidos(eggService.cantidadVendidos);

        return sectionBuySellProducts;
    }
    public FarmProgressDashboard getPropertiesProgressDashboard() {
        /*  1. Obtener la granaja   */
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow( ()-> new RuntimeException("No hay ninguna granja registrada."));

        /*  Serializar clase */
        FarmProgressDashboard farmProgressDashboard = mapper.convertValue(farm, FarmProgressDashboard.class);

        /*  3. Guardar los [días Transcurridos] [Cantidad Huevos] [cantidad Pollos]  */
        farmProgressDashboard.setDiasTranscurridos(getDayPass(farm.getDias())); //30
        farmProgressDashboard.setDiasVida(LifeCycle.DAY_OF_LIFE_FARMER);

        /*  4. Renderizar dashboard.    */
        double porcentajeDiasDeVida = ((farmProgressDashboard.getDiasVida() - farmProgressDashboard.getDiasTranscurridos()) * 1.0 / (farmProgressDashboard.getDiasVida())) * 100;
        farmProgressDashboard.setPorcentajeDiasVida(porcentajeDiasDeVida);

        double porcentajeHuevos = farm.getCantHuevos() * 100.0 / farm.getLimiteHuevos();
        farmProgressDashboard.setPorcentajeHuevos(porcentajeHuevos);

        double porcentajePollos = farm.getCantPollos() * 100.0 / farm.getLimitePollos();
        farmProgressDashboard.setPorcentajePollos(porcentajePollos);

        return farmProgressDashboard;
    }
    public FarmDashboardDto getPropertiesDashboard(){
        /*  1. Obtener la granaja   */
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow( () -> new RuntimeException("No hay ninguna granja registrada."));

        /*  2. Obtener la cantidad de Pollos y Huevos que tiene la granja   */
        int cantidadHuevos = farm.getListEggs().size();
        int cantidadPollos = farm.getListChickens().size();

        /*  Serializar clase */
        FarmDashboardDto farmDashboardDto = mapper.convertValue(farm, FarmDashboardDto.class);

        int diasTranscurridos = getDayPass(farm.getDias());
        String fecha = getDayWithformatDate(diasTranscurridos, "dd 'de' MMMM yy");

        farmDashboardDto.setFecha(fecha);
        farmDashboardDto.setCantHuevos(cantidadHuevos);
        farmDashboardDto.setCantPollos(cantidadPollos);

        return farmDashboardDto;
    }
    public ApiResponse<String> buy(String tipo, int cantidad){
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow( ()-> new RuntimeException("No hay ninguna granja registrada."));

        try{
            if (tipo.equals("chicken")){
                chickenService.buy(farm, cantidad);
            } else if (tipo.equals("egg")) {
                eggService.buy(farm, cantidad);
            }else {
                logger.error("Solicitud denegada. Debe seleccionar  'chicken' o 'egg'");
                throw new IllegalArgumentException("Solicitud denegada. Debe seleccionar  'chicken' o 'egg'");
            }

            return new ApiResponse<>(200, tipo + " comprado correctamente", "success");
        }catch (Exception e){
            logger.error("No se pudo realizar la compra: " + e.getMessage());
            return new ApiResponse<>(500, "No se pudo realizar la compra: " + e.getMessage(), "danger");
        }
    }
    public ApiResponse<String> sell(String tipo, int cantidad){
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow( () -> new RuntimeException("No hay ninguna granja registrada."));

        try{
            if (tipo.equals("chicken")){
                chickenService.sell(farm, cantidad);
            } else if (tipo.equals("egg")) {
                eggService.sell(farm, cantidad);
            }else {
                logger.error("Solicitud denegada. Debe seleccionar 'chicken' o 'egg'.");
                throw new IllegalArgumentException("Solicitud denegada. Debe seleccionar 'chicken' o 'egg'.");
            }

            return new ApiResponse<>(200, "Se vendieron " + cantidad + " " + tipo + " correctamente", "success");
        }catch (Exception e){
            logger.error("No se pudo vender: " + e.getMessage());
            return new ApiResponse<>(500, "No se pudo vender " + tipo + ": " + e.getMessage(), "danger");
        }

    }
    private boolean isGranjaExpirada(Farm farm, int cantidad){
        return cantidad > farm.getDias();
    }
    private void updateChickenStatus(Farm farm){

        for (Chicken chicken : farm.getListChickens()){

            chicken.setDiasDeVida(chicken.getDiasDeVida() - 1);

            if (chicken.getDiasDeVida() < LifeCycle.DAY_OF_LIFE_CHICKEN && (chicken.getDiasDeVida() % chicken.getDiasParaPonerHuevos()) == 0){
                Egg egg = new Egg(null, (LifeCycle.DAY_BECOME_CHICKEN + 1), Store.PRECIO_VENTA_EGG, 0, chicken, farm);
                farm.getListEggs().add(egg);
                eggRepository.save(egg);
            }
        }

    }
    private void removeDeadChickens( Farm farm){
        List<Chicken> pollosAEliminar = new ArrayList<>();

        for (Chicken chicken : farm.getListChickens()){
            if (chicken.getDiasDeVida() <= 0){
                for (Egg egg : chicken.getListEggs()){
                    egg.setChicken(null); // Desvincula el huevo del pollo
                }

                eggRepository.saveAll(chicken.getListEggs());
                pollosAEliminar.add(chicken); // Agrega el pollo a la lista de pollos a eliminar
            }
        }

        farm.getListChickens().removeAll(pollosAEliminar);
        chickenRepository.deleteAll(pollosAEliminar);

        countChickenDeads = pollosAEliminar.size();
    }
    private void updateFarmData(Farm farm, int cantidad){
        /*  Obtener la cantidad de dias Transcurridos   */
        int diasTranscurridos = getDayPass(cantidad);
        farm.setFecha(getDayWithformatDate(diasTranscurridos, "dd/MM/yyyy"));
        farm.setDias(farm.getDias() - cantidad);
    }
    private void saveListEgg(List<Egg> listEgg){
        for (Egg egg : listEgg){
            egg.setFarm(null);
            eggRepository.save(egg);
        }
    }
    private void saveListChicken(List<Chicken> listChicken){
        for (Chicken chicken : listChicken){
            chicken.setFarm(null);
            chickenRepository.save(chicken);
        }
    }
    private void ownerlessFarm(){
        saveListEgg(myFarm.getListEggs());
        saveListChicken(myFarm.getListChickens());
        myFarm.setDias(0);
        farmRepository.save(myFarm);
        logger.warn("El dueño de la granja no se encuentra con vidas.");
        throw new RuntimeException("El dueño de la granja ya no cuenta con vidas. Pollos y Huevos sin dueño.");
    }
    private void verifyCantidadPositiva(int cantidad){
        if (cantidad <= 0){
            logger.error("Error, la cantidad ingresada de ser Entero positivo. Numero ingresado: " + cantidad);
            throw new RuntimeException("Los días ingresados deben ser Enteros Positivos.");
        }
    }
    public ApiResponse<String> updateUserName(String name){
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow(()-> new RuntimeException("Nose encontró ninguna granja registrada"));

        try{
            validateUserName(name);
            farm.setGranjero(name);
            farmRepository.save(farm);

            logger.info("Se acaba de actualizar el nombre de usuario a  '" + name + "'.");

            return new ApiResponse<>(200, "Se ha actualizado el nombre de usuario a '" + name + "'.", "success");
        }catch (Exception e){
            logger.error("No se pudo actualizar el nombre de usuario a  '" + name + "': " + e.getMessage());
            return new ApiResponse<>(500, e.getMessage(), "danger");
        }
    }
    public ApiResponse<String> updateFarmName(String name){
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow(()-> new RuntimeException("Nose encontró ninguna granja registrada"));

        try {
            validateFarmName(name);
            farm.setNombre(name);
            farmRepository.save(farm);
            logger.info("Se acaba de actualizar el nombre de la App a '" + name + "'.");

            return new ApiResponse<>(200, "Se ha actualizado el nombre de la app a  '" + name + "'.", "success");
        }catch (Exception e){
            logger.error("No se pudo actualizar el nombre de la app: " + e.getMessage());
            return new ApiResponse<>(500, e.getMessage(), "danger");
        }
    }
    public ApiResponse<String>  updatePrices(String accion,String tipo, double precio){
        try {
            if (precio >= 1){
                if (accion.equals("buy")){
                    if (tipo.equals("chicken")){
                        chickenPriceToBuy = precio;
                    }else if (tipo.equals("egg")){
                        eggPriceToBuy = precio; //Store.PRECIO_COMPRA_EGG = precio;
                    }else{
                        throw new RuntimeException("Por favor ingrese tipo correcto. 'chicken' or 'egg'.");
                    }
                } else if (accion.equals("sell")) {
                    if (tipo.equals("chicken")){
                        /*Flag: cambiar precio*/
                        chickenPriceToSell = precio;//Store.PRECIO_VENTA_CHICKEN = precio;
                        //chickenService.updatePriceForSell();
                    }else if (tipo.equals("egg")){
                        eggPriceToSell = precio;//Store.PRECIO_VENTA_EGG = precio;
                        //eggService.updatePriceForSell();
                    }else{
                        throw new RuntimeException("Por favor ingrese tipo correcto. 'chicken' or 'egg'.");
                    }
                }else {
                    throw new RuntimeException("Por favor ingrese la accion requerida. 'buy' or 'sell'.");
                }
                return new ApiResponse<>(200, "Se ha actualizado el precio del " + tipo + " a $" + precio + ".", "success");
            }else {
                throw new RuntimeException("El precio debe ser positivo. Por favor, intente nuevamente.");
            }
        }catch (Exception e){
            return new ApiResponse<>(500, e.getMessage(), "danger");
        }
    }
    private void validateUserName(String userName){
        if (userName.length() < 3){
            logger.warn("El valor ingresado no parece ser un nombre. Por favor, intente nuevamente. La cantidad de digitos debe ser mayor o igual a 3.");
            throw new RuntimeException("El valor ingresado no parece ser un nombre. Por favor, intente nuevamente. La cantidad de digitos debe ser mayor o igual a 3.");
        }
    }
    private void validateFarmName(String farmName){
        if (farmName.length() < 3){
            logger.warn("El valor ingresado no parece ser un nombre. Por favor, intente nuevamente. La cantidad de digitos debe ser mayor o igual a 3.");
            throw new RuntimeException("El valor ingresado no parece ser un nombre. Por favor, intente nuevamente. La cantidad de digitos debe ser mayor o igual a 3.");
        }
    }
    private void validateDayOfLife(int dayOfLife){
        if (dayOfLife <= 0){
            logger.warn("Dias de vida inválidos. El valor ingresado debe ser Positivo. Por favor, intente nuevamente. ");
            throw new RuntimeException("Dias de vida inválidos. El valor ingresado debe ser Positivo. Por favor, intente nuevamente. ");
        } else if (dayOfLife < 10) {
            logger.error("Por favor, intente nuevamente. El valor ingresado debe ser mayor o igual a 10.");
            throw new RuntimeException("Por favor, intente nuevamente. El valor ingresado debe ser mayor o igual a 10.");
        }
    }
    private void validateChickensEggsCapacity(int amount){
        if (amount <= 0){
            logger.warn("El valor ingresado debe ser Positivo. Por favor, intente nuevamente.");
            throw new RuntimeException("El valor ingresado debe ser Positivo. Por favor, intente nuevamente.");
        } else if (amount < 10) {
            logger.warn("Por favor, intente nuevamente. Se recomienda tener tener una capacidad de 10 o más.");
            throw new RuntimeException("Por favor, intente nuevamente. Se recomienda tener tener una capacidad de 10 o más.");
        }
    }
    public ApiResponse<String> updatePerfil(String userName, String farmName, int dayOfLife, int eggsCapacity, int chickensCapacity) {
        Farm farm = farmRepository.findAll().stream().findFirst().orElseThrow(()-> new RuntimeException("Nose encontró ninguna granja registrada"));

        try{
            validateUserName(userName);
            validateFarmName(farmName);
            validateDayOfLife(dayOfLife);
            validateChickensEggsCapacity(chickensCapacity);
            validateChickensEggsCapacity(eggsCapacity);

            farm.setGranjero(userName);
            farm.setNombre(farmName);
            LifeCycle.DAY_OF_LIFE_FARMER = dayOfLife;
            farm.setDias(LifeCycle.DAY_OF_LIFE_FARMER);
            farm.setLimiteHuevos(eggsCapacity);
            farm.setLimitePollos(chickensCapacity);

            farmRepository.save(farm);
            return new ApiResponse<>(200, "Se ha actualizado el perfil exitosamente.", "success");
        }catch (Exception e){
            logger.warn("No puedo actualizarse el perfil. Detail: " + e.getMessage());
            return new ApiResponse<>(500, e.getMessage(), "danger");
        }
    }

    public void sellExcedent(Farm farm, int cantidad){
        switch (verifyExcess(farm)){
            case "Both":
                System.out.printf("Desarrollar logica");
                break;
            case "Eggs":
                int newEggs = (farm.getLimiteHuevos() - farm.getCantHuevos());
                int excedentEggs = farm.getListEggs().size() - farm.getLimiteHuevos();
                eggService.sellExcedent(farm, newEggs, excedentEggs); /* Revisar Stock Actual disponible aún + los nuevos huevos agregados a la granja*/
                break;
            case "Chickens":
                int newChickens = (farm.getLimitePollos() - farm.getCantPollos());
                int excedentChickens = farm.getListChickens().size() - farm.getLimitePollos();
                chickenService.sellExcedent(farm, newChickens, excedentChickens);
                break;
            default:
                logger.info("Stock disponible. Entro al default del switch");
                /*  Actualizando la cantidad de Huevos y Pollos. */
                farm.setCantHuevos(eggRepository.findAll().size());
                farm.setCantPollos(chickenRepository.findAll().size());
                break;
        }


    }
    public String verifyExcess (Farm farm){
        if (farm.getCantHuevos() > farm.getLimiteHuevos() && farm.getCantPollos() > farm.getLimitePollos()){
            logger.info("Supero el Stock de Huevos y Pollos");
            logger.info("Stock actual de Huevos: " + farm.getCantHuevos() + " de " + farm.getLimiteHuevos());
            logger.info("Stock actual de Pollos: " + farm.getCantPollos() + " de " + farm.getLimitePollos());

            return "Both";
        }else if (farm.getCantHuevos() + (farm.getListEggs().size() - farm.getCantHuevos() ) > farm.getLimiteHuevos()){
            logger.info("Supero el Stock de Huevos");
            logger.info("Stock actual de Huevos: " + farm.getCantHuevos() + " . Los " + farm.getCantPollos() + " Pollos acaban de poner " + (farm.getListEggs().size() - farm.getCantHuevos()) + " nuevos huevos.");
            logger.info("Stock final de Huevos: " + farm.getListEggs().size() + " de " + farm.getLimiteHuevos());
            logger.info("Se guardaran " + (farm.getLimiteHuevos() - farm.getCantHuevos()) + " huevos. Y se venderan " + (farm.getListEggs().size() - farm.getLimiteHuevos()));

            return "Eggs";
        } else if (farm.getCantPollos() + (farm.getListChickens().size() - farm.getCantPollos()) > farm.getLimitePollos()) {
            logger.info(":::Supero el Stock de Pollos:::");
            logger.info("Stock actual de Pollos: " + farm.getCantPollos() + " . ");
            logger.info("Stock final de Pollos: " + farm.getListChickens().size() + " de " + farm.getLimitePollos());
            logger.info("Se guardaran " + (farm.getLimitePollos() - farm.getCantPollos()) + " pollos. Y se venderan " + (farm.getListChickens().size() - farm.getLimitePollos()));

            return "Chickens";
        }else {
            logger.info("Stock Disponible ...");
            return "None";
        }
    }
    public ApiResponse<String> pasarDias(int cantidad){
        myFarm = getFarm(1L);
        try{
            verifyCantidadPositiva(cantidad);

            if (!isGranjaExpirada(myFarm, cantidad)){
                for (int i = 0; i < cantidad; i++){
                    updateChickenStatus(myFarm);
                    removeDeadChickens(myFarm);
                    eggService.diasEnConvertirseEnPollo(myFarm);
                }
            } else{
                ownerlessFarm();
            }
            updateFarmData(myFarm, cantidad);

            chickenService.updatePrice(chickenPriceToBuy, chickenPriceToSell);
            eggService.updatePrice(eggPriceToSell, eggPriceToBuy);

            sellExcedent(myFarm,cantidad);  // revisar si usar el getCanPollos o el getListChickens.size()

            farmRepository.save(myFarm);

            return new ApiResponse<>(200, "Acaban de pasar " + cantidad + " días.", "success");
        }catch (Exception e){
            return new ApiResponse<>(500, e.getMessage(), "danger");
        }
    }




































}
