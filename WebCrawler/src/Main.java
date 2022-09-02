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
        String out = "Author,Path,Name,Servings,Ingredients,Instructions\n";

        //get source files in an arrayList of strings
        ArrayList<String> recipesUrls = getSource();

        //debug only
        for(int i = 0; i < recipesUrls.size(); i++){
            System.out.println(i + " " + recipesUrls.get(i));
        }

        //get information and put into comma separated string
        for(int i = 0; i < numOfRecipes; i++){
            out += getRecipe(recipesUrls.get(i)) + "\n";
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
        String url = "https://www.surlatable.com/recipes/?srule=best-matches&start=0&sz=24";

        //create urls
        for(int i = 0; i < numOfRecipes; i+=24){
            urls.add("https://www.surlatable.com/recipes/?srule=best-matches&start=" + i + "&sz=24");
        }

        HttpClient client = HttpClient.newHttpClient();
        
        //holds all urls for all recipes
        ArrayList<String> recipeUrls = new ArrayList<String>();

        //get request each site page and grab 24 recipe urls
        for(int i = 0; i < urls.size();i++ ) {
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
    
    public static String getRecipe(String url){
        String recipe = "Recipe, name";
        

        
        return recipe;
    }

}