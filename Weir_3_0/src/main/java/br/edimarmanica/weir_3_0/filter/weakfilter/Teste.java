/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir_3_0.filter.weakfilter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author edimar
 */
public class Teste {
    public static void main(String[] args) {
        int nrcores = 4;
        List<Integer> list = new ArrayList<>();
        for(int i=1; i<=199; i++){
            list.add(i);
        }
        
        int resto = list.size()%nrcores;
        int partes;
        if (resto == 0){
            partes = list.size()/nrcores;
        }else{
            partes = (list.size()/nrcores)+1;
        }
        
        for(int i=0; i<nrcores; i++){
            System.out.println("i: "+i);
            int start = i*partes;
            int end = start+partes;
            if (end > list.size()){
                end = list.size();
            }
            System.out.print("\t");
            for(int j = start; j<end; j++){
                System.out.print(list.get(j)+",");
            }
            System.out.println("");
            
            System.out.println("Aki: "+(5/3));
        }
    }
}
