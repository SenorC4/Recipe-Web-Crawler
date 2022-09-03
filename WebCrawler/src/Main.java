import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main {

    ///change this for different number of recipes
    static int numOfRecipes = 96;
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
            int start;
            int end;

            //Delay for the sake of Sur La Tables infrastructure
            TimeUnit.SECONDS.sleep(10);

            //get recipe site
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(recipesUrls.get(i))).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //get name
            System.out.println(i + " 1");
            String name;
            start = response.body().indexOf("recipe-name") + 13;
            end = response.body().indexOf("</h1>", start);
            name = response.body().substring(start, end);

            name = formatRecipe(name);

            recipe+= name + ",";


            //get author
            System.out.println(i + " 2");
            start = response.body().indexOf("By ", end) + 3;
            end = response.body().indexOf("\n", start);
            String author = response.body().substring(start, end) + ",";

            author = formatRecipe(author);
            recipe += author + ",";
            System.out.println(author);

            //get path
            System.out.println(i + " 3");
            start = response.body().lastIndexOf("\"@id\":\"https://www.surlatable.com/recipes/", end) + 33;
            end = response.body().indexOf("name", start) - 3;
            String path = "home" + response.body().substring(start, end) + name + ",";
            System.out.println(path);
            System.out.println(name);
            recipe += path;


            //get servings
            System.out.println(i + " 4");
            String servings;

            if(response.body().indexOf("recipe-details-serves") > 1) {
                start = response.body().indexOf("recipe-details-serves") + 29;
                end = response.body().indexOf("</div>\n", start)-1;

                servings = response.body().substring(start, end);
                servings = formatRecipe(servings);
                recipe += servings + ",";
            }else{
                servings = "1,";
                recipe += servings;
            }



//            if(response.body().indexOf("ervings", start) > 1)
//                end = response.body().indexOf("ervings", start) - 2;
//            else if(response.body().indexOf(" cups", start) > 1){
//                end = response.body().indexOf(" cups", start);
//            }else{
//                servings = "1";
//            }



            //get ingredients
            System.out.println(i + " 5");
            start = response.body().indexOf("recipe-details-ingredients") + 34;
            end = response.body().indexOf("</div>\n", start);
            String ingredients = response.body().substring(start, end) + ",";

            ingredients = formatRecipe(ingredients);

            recipe += ingredients + ",";

            //get Instructions
            System.out.println(i + " 6");
            start = response.body().indexOf("recipe-details-procedure") + 27;
            end = response.body().indexOf("</div>\n", start);
            String instructions = response.body().substring(start, end) + ",";

            instructions = formatRecipe(instructions);

            recipe += instructions;

            //put one recipe line into main string that gets written
            out += recipe + "\n";

            //debug only
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
            TimeUnit.SECONDS.sleep(10);

        }
        return recipeUrls;
    }

    public static String formatRecipe(String stuff){
        stuff = stuff.replaceAll("\\<.*?\\>" , "").replace("\n", " ").replace(",", "");

        //i hate this, this sucks, please give me better libraries for html escape characters. Oracle I love you, but no
        stuff = stuff.replaceAll("&rsquo;", "'");
        stuff = stuff.replaceAll("&#38;#176;", "°");
        stuff = stuff.replaceAll("&#38;#38;", "&");
        stuff = stuff.replaceAll("&#38;amp;", "&");
        stuff = stuff.replaceAll("&amp;", "&");
        stuff = stuff.replaceAll("&#38;", "&");


        return stuff;
    }

}