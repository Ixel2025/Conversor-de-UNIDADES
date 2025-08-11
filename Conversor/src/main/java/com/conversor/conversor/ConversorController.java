package com.conversor.conversor;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.LinkedList;


public class ConversorController {

    @FXML
    private TextField valorField;

    @FXML
    private ComboBox<String>unidadOrigenCombo;
    @FXML
    private ComboBox<String>unidadDestinoCombo;

    @FXML
    private  Label resultadoLabel;

    @FXML
    private TextArea historialArea;

    private static final Pattern NUMERO_PATTERN=Pattern.compile("-?\\d*\\.?\\d*");
    private static final DateTimeFormatter formatter=DateTimeFormatter.ofPattern("HH:mm:ss");
    private LinkedList<String>historial=new LinkedList<>();
    private static final int MAX_HISTORIAL=10;

    @FXML
    public void initialize(){
        configurarValidacionNumerica(valorField);
        inicializarCombos();
        historialArea.setEditable(false);


    }
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


    private void inicializarCombos(){

        //Unidades de longitud

        unidadOrigenCombo.getItems().addAll(
                "Metros","Centimetros","Pulgadas", "Pies","Yardas",
                "Kilogramos","Gramos","Libras","Onzas","Celsius","Farenheit",
                "Kelvin");


        unidadDestinoCombo.getItems().addAll(
                "Metros","Centimetros","Pulgadas", "Pies","Yardas",
                "Kilogramos","Gramos","Libras","Onzas","Celsius","Farenheit",
                "Kelvin"

        );

    }

    private void configurarValidacionNumerica(TextField textField){

        TextFormatter<String>formatter=new TextFormatter<>(change-> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            if (newText.equals("-")) {
                return change;
            }
            if (newText.equals(".") || newText.equals("-.")) {
                return change;
            }
            if (NUMERO_PATTERN.matcher(newText).matches()) {
                long countDots = newText.chars().filter(ch -> ch == '.').count();
                if (countDots <= 1) {
                    return change;
                }
            }
            return null;
        });

        textField.setTextFormatter(formatter);
        textField.setTooltip(new Tooltip("Ingrese solo numeros(ej: 123,-20.10,0.50"));

    }
    @FXML
    private void convertir(){
        try{
            if (valorField.getText().trim().isEmpty()){
                mostrarError("Ingrese el valor que desea convertir");
                return;
            }
            if (unidadOrigenCombo.getValue()==null||unidadDestinoCombo.getValue()==null){
                mostrarError("Por favor. selecionar las unidades de origen y destino)");
                return;
            }
            //Para validar que no sean la misma unidad
            if(unidadOrigenCombo.getValue().equals(unidadDestinoCombo.getValue())){
                mostrarError("No se puede convertir a la misma unidad");
                return;
            }
            //Para validar que las unidades sean del mismo tipo, si es tiempo que la unidad a convertir sea tiempo

            if (!sonUnidadesCompatibles(unidadOrigenCombo.getValue(), unidadDestinoCombo.getValue())) {
                mostrarError("Las unidades seleccionadas no son del mismo tipo");
                return;
            }
            double valor = Double.parseDouble(valorField.getText().trim());
            double resultado =realizarConversion(valor,unidadOrigenCombo.getValue(),
                    unidadDestinoCombo.getValue());

            //para mostrar el resultado

            resultadoLabel.setText(String.format("%.4f",resultado));

            //para agregar al historial
            String timestamp= LocalDateTime.now().format(formatter);
            String conversionStr= String.format("[%s] %.2f %s → %s = %.4f%n",
                    timestamp,valor,unidadOrigenCombo.getValue(),unidadDestinoCombo.getValue(),resultado);

            agregarAlHistorial(conversionStr);
        } catch (NumberFormatException e){
            mostrarError("Por favor, ingrese un numero valido");
        }catch (IllegalArgumentException e){
            mostrarError(e.getMessage());
        } catch (Exception e){
            mostrarError("Error inesperado: "+ e.getMessage());
        }

    }

    private boolean sonUnidadesCompatibles(String unidadOrigen, String unidadDestino)
    {
        //Determina si las unidades son del mismo tipo(longitud,peso temperatura
        String[] longitudes={"Metros","Centimetros","Pulgadas","Pies","Yardas"};
        String[] pesos={"Kilogramos","Gramos","Libras","Onzas"};
        String[] temperaturas ={ "Celsius","Farenheit","Kelvin"};

        boolean origenEsLongitud = contiene (longitudes, unidadOrigen) ;
        boolean destinoEsLongitud= contiene (longitudes, unidadDestino);
        if (origenEsLongitud && destinoEsLongitud) return true;

        boolean origenEsPeso = contiene (pesos, unidadOrigen);
        boolean destinoEsPeso = contiene (pesos,unidadDestino);
        if(origenEsPeso && destinoEsPeso) return true;

        boolean originEsTemp = contiene (temperaturas,unidadOrigen);
        boolean destinoEsTemp = contiene(temperaturas,unidadDestino);
        if (originEsTemp && destinoEsTemp) return true;

        return false;
    }

    private boolean contiene (String[] array,String valor){
        for (String s : array){
            if(s.equals(valor)) return true;
        }
        return false;

    }

    private double realizarConversion(double valor, String unidadOrigen, String unidadDestino) {
        // Primero convertir a la unidad base y luego a la unidad destino
        double valorEnBase = convertirABase(valor, unidadOrigen);
        return convertirDesdeBase(valorEnBase, unidadDestino);
    }

    private double convertirABase(double valor,String unidadOrigen){
        switch (unidadOrigen){
            case "Metros": return valor;
            case "Centimetros" : return valor / 100;
            case "Pulgadas" : return valor *0.0254;
            case "Pies" : return  valor * 0.3048;
            case "Yardas" : return valor * 0.9144;

            case "Kilogramos": return valor;
            case "Gramos": return valor / 1000;
            case "Libras": return valor * 0.453592;
            case "Onzas": return valor * 0.0283495;


            case "Celsius": return valor;
            case "Fahrenheit": return (valor - 32) * 5/9;
            case "Kelvin": return valor - 273.15;

            default: throw new IllegalArgumentException("Unidad de origen no valida");
        }
    }
    private double convertirDesdeBase(double valor, String unidadDestino) {
        switch (unidadDestino) {
            // Longitud
            case "Metros": return valor;
            case "Centímetros": return valor * 100;
            case "Pulgadas": return valor / 0.0254;
            case "Pies": return valor / 0.3048;
            case "Yardas": return valor / 0.9144;

            // Peso
            case "Kilogramos": return valor;
            case "Gramos": return valor * 1000;
            case "Libras": return valor / 0.453592;
            case "Onzas": return valor / 0.0283495;

            // Temperatura
            case "Celsius": return valor;
            case "Fahrenheit": return (valor * 9/5) + 32;
            case "Kelvin": return valor + 273.15;

            default: throw new IllegalArgumentException("Unidad de destino no válida");
        }
    }
    private void agregarAlHistorial(String conversion){
        historial.addFirst(conversion);

        if (historial.size()>MAX_HISTORIAL){
            historial.removeLast();
        }

        historialArea.clear();
        historial.forEach(item->historialArea.appendText(item));

    }
    @FXML
    private void limpiar() {
        valorField.clear();
        unidadOrigenCombo.getSelectionModel().clearSelection();
        unidadDestinoCombo.getSelectionModel().clearSelection();
        resultadoLabel.setText("0");
        historial.clear();
        historialArea.clear();
    }


}