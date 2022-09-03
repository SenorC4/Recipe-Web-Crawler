import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.net.URLDecoder;


public class Main {

    ///change this for different number of recipes
    static int numOfRecipes = 4;
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
        for(int i = 0; i < numOfRecipes; i++){
            //name,Author,Path,Name,Servings,Ingredients,Instructions
            String recipe = "";
            int start = 0;
            int end = 0;

            //get recipe site
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(recipesUrls.get(i))).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //get name
            String name = "";
            start = response.body().indexOf("recipe-name") + 13;
            end = response.body().indexOf("</h1>", start);
            name = response.body().substring(start, end);
            recipe+= name + ",";


            //get author
            start = response.body().indexOf("By ", end) + 3;
            end = response.body().indexOf("\n", start);
            String author = response.body().substring(start, end) + ",";
            //author = URLDecoder.decode(author);
            author = author.replaceAll("\\<.*?\\>" , "").replace("\n", " ").replace(",", "");
            recipe += author + ",";


            //get path
            start = response.body().lastIndexOf("\"@id\":\"https://www.surlatable.com/recipes/", end) + 33;
            end = response.body().indexOf("\",\"", start);
            recipe += "home" + response.body().substring(start, end) + name + ",";

            //get servings
            if(response.body().indexOf("Makes") > 1) {
                start = response.body().indexOf("Makes") + 6;

                if(response.body().indexOf("ervings", start) > 1)
                    end = response.body().indexOf("ervings", start) - 2;
                else if(response.body().indexOf(" cups", start) > 1){
                    end = response.body().indexOf(" cups", start);
                }

                recipe += response.body().substring(start, end) + ",";
            }else{
                recipe += "1,";
            }

            //get ingredients
            start = response.body().indexOf("recipe-details-ingredients") + 34;
            end = response.body().indexOf("<br><br>", start);
            String ingredients = response.body().substring(start, end) + ",";

            ingredients = ingredients.replaceAll("\\<.*?\\>" , "").replace("\n", " ").replace(",", "");

            recipe += ingredients + ",";

            //get Instructions
            start = response.body().indexOf("recipe-details-procedure") + 27;
            end = response.body().indexOf("<br><br>\n", start);
            String instructions = response.body().substring(start, end) + ",";

            instructions = instructions.replaceAll("\\<.*?\\>" , "").replace("\n", " ").replace(",", "");

            recipe += instructions;

            //fix html encode for stupid names (L&eacute'ku&eacute;)


            out += recipe + "\n";

            System.out.println(i + ": " + out);
        }

        //Write string to csv
        FileOutputStream fs = new FileOutputStream("Recipes.csv");
        PrintWriter pw = new PrintWriter(fs);
        pw.println(out);
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
            System.out.println("Getting first file");
            int start = 0;
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
            TimeUnit.SECONDS.sleep(10);

        }
        return recipeUrls;
    }

    public static String formatRecipe(String instructions){


        return instructions;
    }

}