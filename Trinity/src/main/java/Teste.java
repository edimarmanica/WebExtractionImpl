
import br.edimarmanica.configuration.Paths;
import br.edimarmanica.dataset.Site;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author edimar
 */
public class Teste {

    public static void main(String[] args) {
        String st = "Comedy|Cri25me|Dra,ma|Thr.il$ler";
        System.out.println(st.replaceAll("[^(a-zA-Z)\\d\\.]", ""));
        System.out.println("aki");
        Site site = br.edimarmanica.dataset.swde.book.Site.ADEBOOKS;
        System.out.println(Paths.PATH_BASE+site.getPath());

    }
}
