package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import sun.rmi.runtime.Log;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Api( description="API pour es opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;


    //Récupérer la liste des produits

    @RequestMapping(value = "/Produits", method = RequestMethod.GET)

    public MappingJacksonValue listeProduits() {

        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }


    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")

    public Product afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est introuvable.");

        return produit;
    }




    //ajouter un produit
    @PostMapping(value = "/Produits")

    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {
        if(product.getPrix() == 0){
            throw new ProduitGratuitException("Impossible d'ajouter le produit, le prix est à zéro.");
        }
        else{
            Product productAdded =  productDao.save(product);

            if (productAdded == null)
                return ResponseEntity.noContent().build();

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(productAdded.getId())
                    .toUri();

            return ResponseEntity.created(location).build();
        }

    }

    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

        productDao.delete(id);
    }

    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {
        if(product.getPrix() == 0){
            throw new ProduitGratuitException("Impossible de mettre à jour le produit, le prix est à zéro.");
        }
        else{
            productDao.save(product);
        }

    }


    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {

        return productDao.chercherUnProduitCher(400);
    }

    @GetMapping(value = "test/produitsOrderAsc")
    public List<Product> trierProduitsParOrdreAlphabetique() {

        return productDao.findAllByOrderByNomAsc();

    }

    @GetMapping(value = "AdminProduits")
    public Map calculerMargeProduit() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Product> products = productDao.findAll();


        for (Product product : products) {
            float value = product.getPrix() - product.getPrixAchat();
            result.put(product.toString(),Math.round(value * 100.0) / 100.0);
        }

        return result;

    }

    @GetMapping(value = "AdminProduits/{id}")
    public Map calculerMargeProduitById(@PathVariable int id) {
        Map<String, Object> result = new LinkedHashMap<>();
        Product product = productDao.findById(id);

        if(product==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est introuvable.");

        float value = product.getPrix() - product.getPrixAchat();
        result.put(product.toString(),Math.round(value * 100.0) / 100.0);

        return result;

    }

    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/test/{id}")

    public String test(@PathVariable int id) {

        String url = "http://stsi-router.int.apave.grp/router/"+id;
        List<Pair<String,String>> params = new ArrayList<>();
        List<Pair<String,String>> headers= new ArrayList<>();
        String body = "";

        headers.add(new Pair("context-applicationSource","GAS"));
        headers.add(new Pair("context-flowName","ORGA-109-901"));

        httpRequestContent request;
        try{
            request = sendGet(url, params,headers, body);
        }
        catch(IOException e){
            throw new ProduitIntrouvableException(e.getMessage());
        }

        //return getResponsable(ret) ;
        return getResponsable(request.getBody());
    }

    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/test3/{id}")
    public String test3(@PathVariable int id) {

        String url = "http://stsi-router.int.apave.grp/router/"+id;
        List<Pair<String,String>> params = new ArrayList<>();
        List<Pair<String,String>> headers= new ArrayList<>();
        String body = "";

        headers.add(new Pair("context-applicationSource","GAS"));
        headers.add(new Pair("context-flowName","ORGA-109-901"));

        httpRequestContent request = new httpRequestContent(url, headers, params, body,true);
        try{
            request.build();
        }
        catch(IOException e){
            throw new ProduitIntrouvableException(e.getMessage());
        }

        //return getResponsable(ret) ;
        return getResponsable(request.getBody());
    }


    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/test2/{id}")
    public String test2(@PathVariable int id, @RequestParam("val") String val)throws IOException {
        System.out.println("HEYA");
        String url = "http://stsi-router.int.apave.grp/router/"+id;
        URL ur = new URL(url);
        HttpURLConnection con = (HttpURLConnection) ur.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);

        con.setRequestProperty("context-applicationSource","GAS");
        con.setRequestProperty("context-flowName","ORGA-"+val+"-901");

        BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        //Récupération du responsable dans la grappe
        return getResponsable(sb.toString());
    }


    private static String getResponsable(String json){
        String responsable = "";

        if(isJSONValid(json)){
            //Conversion du string en object jSON
            JsonParser parser = new JsonParser();
            JsonElement jsonTree = parser.parse(json.toString());

            //Récupération de la grappe idResponsable
            if(jsonTree.isJsonObject()){
                JsonObject jsonObject = jsonTree.getAsJsonObject();
                JsonElement respTree = jsonObject.get("idResponsable");

                if(respTree != null ) {
                    if( respTree.isJsonObject()){
                        JsonObject objResp = respTree.getAsJsonObject();
                        JsonElement codeCollab = objResp.get("codeCollaborateur");
                        if(codeCollab != null ) {
                            responsable = codeCollab.toString();
                        }

                    }
                }
            }
        }

        return responsable.replace("\"", "");
    }

    public static boolean isJSONValid(String jsonInString) {
        try {
            Gson gson = new Gson();
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    public httpRequestContent sendGet(String url,List<Pair<String,String>> parameters, List<Pair<String,String>> headers, String body) throws IOException{

        httpRequestContent request = new httpRequestContent(true);

        URL ur = new URL(url);
        HttpURLConnection con = (HttpURLConnection) ur.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);

        //Ajout des headers
        if(headers != null){
            for (Pair<String, String> header : headers) {
                con.setRequestProperty(header.getKey(),header.getValue());
            }
        }

        //Ajout des paramètres
        if(!parameters.isEmpty()){
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            out.flush();
            out.close();
        }

        int status = con.getResponseCode();
        request.setStatus(status);
        System.out.println("Status:"+status);


        //Récupération des headers
        for (Map.Entry<String, List<String>> entries : con.getHeaderFields().entrySet()) {
            String values = "";
            for (String value : entries.getValue()) {
                values += value + ",";
            }
            System.out.println("Response"+ entries.getKey()+", "+values);
            request.setHeaders(entries.getKey(),values);

        }

        //récupération du body  https://stackoverflow.com/questions/25011927/how-to-get-response-body-using-httpurlconnection-when-code-other-than-2xx-is-re
        BufferedReader br = (status <400)? new BufferedReader(new InputStreamReader((con.getInputStream()))) :new BufferedReader(new InputStreamReader((con.getErrorStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        br.close();
        con.disconnect();

        request.setBody(sb.toString());
        return request;
    }

    public String sendGet2(String url, List<Pair<String,String>> parameters, List<Pair<String,String>> headers, String body) throws IOException {

        URL ur = new URL(url);
        HttpURLConnection con = (HttpURLConnection) ur.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        //Ajout des headers
        if(headers != null){
            for (Pair<String, String> header : headers) {
                con.setRequestProperty(header.getKey(),header.getValue());
            }
        }

        //Ajout des paramètres
        if(parameters != null){
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            out.flush();
            out.close();
        }

        int status = con.getResponseCode();

        BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        br.close();
        con.disconnect();
        return sb.toString();

    }


    /*@GetMapping(value = "AdminProduits")
    public List<Map> calculerMargeProduit() {
        List<Map> productToStr = new ArrayList<Map>();
        List<Product> products = productDao.findAll();


        for (Product product : products) {
            DecimalFormat df = new DecimalFormat("#.##");
            float value = product.getPrix() - product.getPrixAchat();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put(product.toString(),df.format(value ));
            productToStr.add(result);
        }

        return productToStr;

    }*/



}
