package brain.brainstormer.api;

public class Main {
    public static void main(String[] args) {
        GiphyApi giphyApi = new GiphyApi();

        // Test search query
        String query = "cat";  // You can replace "cat" with any search term
        System.out.println("Searching for GIFs with query: " + query);

        try {
            String gifUrl = giphyApi.searchGif(query);
            System.out.println("Found GIF URL: " + gifUrl);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to retrieve GIF. Error: " + e.getMessage());
        }
    }
}
