/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edimarmanica.weir.util;
import java.util.HashSet;
import java.util.Set;



/**
 *
 * @author edimar
 */
public class Conjuntos<T> {
    
    
    public Set<T> intersection(Set<T> c1, Set<T> c2){
        Set<T> aux = new HashSet<>();
        aux.addAll(c1);
        aux.retainAll(c2);
        return aux;
    } 
    
    
}
