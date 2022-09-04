import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main {

    ///change this for different number of recipes
    static int numOfRecipes = 2167;
    //Change this for different number of recipes

    public static void main(String[] args) throws IOException, InterruptedException {
        String out = "Name,Author,Path,Servings,Ingredients,Instructions\n";

        //get source files in an arrayList of strings
        ArrayList<String> recipesUrls = getSource();

        //debug only
        for(int i = 0; i < recipesUrls.size(); i++){
            System.out.println(i + " " + recipesUrls.get(i));
        }


        //get information and put into comma separated string
        for(int i = 0; i < numOfRecipes; i++) {
            //name,Author,Path,Name,Servings,Ingredients,Instructions
            String recipe = "";
            int start = 0;
            int end = 0;

            //Delay for the sake of Sur La Tables infrastructure
            //TimeUnit.SECONDS.sleep(10);

            //get recipe site
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(recipesUrls.get(i))).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(recipesUrls.get(i));

            //get name
            String name = "";

            if(response.body().indexOf("recipe-name") > 1 && response.body().indexOf("/h1") > 1) {
                start = response.body().indexOf("recipe-name") + 13;
                end = response.body().indexOf("/h1", start) - 1;
                name = response.body().substring(start, end);
            }else{
                name = "FAILED TO GET NAME";
            }
            name = formatRecipe(name);

            System.out.println(name);

            recipe += name + ",";


            //get author
            String author = "";
            System.out.println(i + " 2");
            if (response.body().indexOf("By ", end) > 1 && response.body().indexOf("\n", start) > 1) {
                start = response.body().indexOf("By ", end) + 3;
                end = response.body().indexOf("\n", start);
                author = response.body().substring(start, end) + ",";
            } else {
                author = "No Author";
            }

            author = formatRecipe(author);
            System.out.println(author);
            recipe += author + ",";
            System.out.println(author);

            //get path
            String path = "";
            System.out.println(i + " 3");
            if (response.body().lastIndexOf("\"@id\":\"https://www.surlatable.com/recipes/", end) > 1 && response.body().indexOf("name", start) > 1){
                start = response.body().lastIndexOf("\"@id\":\"https://www.surlatable.com/recipes/", end) + 33;
                end = response.body().indexOf("name", start) - 3;
                path = "home" + response.body().substring(start, end) + name;
                System.out.println(path);
                System.out.println(name);
            }else{
                path = "home/recipes/" + name;
            }
            System.out.println(path);
            recipe += path + ",";


            //get servings
            System.out.println(i + " 4");
            String servings;

            if(response.body().indexOf("recipe-details-serves") > 1 && response.body().indexOf("</div>\n", start) > 1)  {
                start = response.body().indexOf("recipe-details-serves") + 30;
                end = response.body().indexOf("</div>\n", start)-1;
                servings = response.body().substring(start, end);
                servings = formatRecipe(servings);
            }else{
                servings = "1 serving";
            }
            System.out.println(servings);
            recipe += servings + ",";


            //get ingredients
            System.out.println(i + " 5");
            String ingredients = "";

            if(response.body().indexOf("recipe-details-ingredients") > 1 && response.body().indexOf("</div>\n", start) > 1) {
                start = response.body().indexOf("recipe-details-ingredients") + 28;
                end = response.body().indexOf("</div>\n", start);
                ingredients = response.body().substring(start, end) + ",";
            }else{
                ingredients = "FAILED TO GET INGREDIENTS,";
            }

            ingredients = formatRecipe(ingredients);

            recipe += ingredients + ",";

            //get Instructions
            System.out.println(i + " 6");
            String instructions = "";

            if(response.body().indexOf("recipe-details-procedure") > 1 && response.body().indexOf("</div>\n", start) > 1) {
                start = response.body().indexOf("recipe-details-procedure") + 26;
                end = response.body().indexOf("</div>\n", start);
                instructions = response.body().substring(start, end) + ",";
            }else{
                instructions = "FAILED TO GET INSTRUCTIONS,";
            }

            instructions = formatRecipe(instructions);

            System.out.println(instructions);

            recipe += instructions;

            //put one recipe line into main string that gets written
            out += recipe + "\n";

            //debug only
            System.out.println(i + ": " + recipe);
        }

        //Write string to csv
        FileOutputStream fs = new FileOutputStream("Recipes.csv");
        OutputStreamWriter pw = new OutputStreamWriter(fs, StandardCharsets.UTF_8);
        pw.write(out);
        pw.close();

    }

    public static ArrayList<String> getSource() throws IOException, InterruptedException {

        //arraylist to hold all urls for different pages ex 1-24 25-48...
        ArrayList<String> urls = new ArrayList<String>();

        //create urls
        for(int i = 0; i < numOfRecipes; i+=24){
            urls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=" + i + "&sz=24");
        }

        HttpClient client = HttpClient.newHttpClient();
        
        //holds all urls for all recipes
        ArrayList<String> recipeUrls = new ArrayList<String>();

        //get request each site page and grab 24 recipe urls
        for(int i = 0; i < urls.size();i++ ) {
            System.out.println("Getting 24 recipes site file");
            int start;
            int end = 0;
            //get request to grab site.main
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urls.get(i))).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //grab recipe urls from sites
            for(int j = 0; j < 24; j++) {

                start = response.body().indexOf("thumb-link", end) + 18;
                end = response.body().indexOf('"', start);

                recipeUrls.add(response.body().substring(start, end));

            }
            //Delay for the sake of Sur La Tables infrastructure
            //TimeUnit.SECONDS.sleep(10);

        }
        return recipeUrls;
    }

    public static String formatRecipe(String stuff){
        stuff = stuff.replaceAll("\\<.*?\\>" , "");

        stuff = stuff.replace("&nbsp;", "");

        stuff = stuff.replace("\r\n", " ");
        stuff = stuff.replace("\n", " ");
        stuff = stuff.replace("\r", " ");

        stuff = stuff.replace(",", "");

        //I hate this, this sucks, please give me better libraries for html escape characters.
        stuff = stuff.replaceAll("&rsquo;", "\'");
        stuff = stuff.replaceAll("&#8217;", "\'");
        stuff = stuff.replaceAll("&lsquo;", "\'");
        stuff = stuff.replaceAll("&#38;#176;", " degrees ");
        stuff = stuff.replaceAll("&#176;", " degrees ");
        stuff = stuff.replaceAll("&deg;", " degrees ");

        stuff = stuff.replaceAll("&#38;#38;", "&");
        stuff = stuff.replaceAll("&#38;amp;", "&");
        stuff = stuff.replaceAll("&amp;", "&");
        stuff = stuff.replaceAll("&#38;", "&");

        stuff = stuff.replaceAll("&eacute;", "e");
        stuff = stuff.replaceAll("&#234;", "e");
        stuff = stuff.replaceAll("&amp;#234;", "e");

        stuff = stuff.replaceAll("&#232;", "e");
        stuff = stuff.replaceAll("&#233;", "e");
        stuff = stuff.replaceAll("&amp;#233;", "e");
        stuff = stuff.replaceAll("&#224;", "a");

        stuff = stuff.replaceAll("&#188;", " 3/4");
        stuff = stuff.replaceAll("&#189;", " 1/2");
        stuff = stuff.replaceAll("&#190;", " 3/4");

        stuff = stuff.replaceAll("&#8531;", " 1/3");
        stuff = stuff.replaceAll("&#8532;", " 2/3");
        stuff = stuff.replaceAll("&#8539;", " 1/8");

        stuff = stuff.replaceAll("&lt;i&gt", "");


        stuff = stuff.replaceAll("&#8212;", "-");
        stuff = stuff.replaceAll("&#8211;", "-");
        stuff = stuff.replaceAll("&#8220;", "\"");
        stuff = stuff.replaceAll("&#8221;", "\"");
        stuff = stuff.replaceAll("&#34;", "\"");



        return stuff;
    }

}