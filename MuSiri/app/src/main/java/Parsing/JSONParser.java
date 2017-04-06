package Parsing;

public class JSONParser
{
    private String jsonString;

    public JSONParser()
    {

    }

    public JSONParser(String jsonString)
    {
        this.jsonString = jsonString;
    }

    public void setJSONString(String jsonString)
    {
        this.jsonString = jsonString;
    }

    /* TODO: change the method so it will give the index field and not only the first every time */

    public String getFieldValue(String field, int index)
    {
        String value = "";
        String fixedField = "\"" + field + "\""; // making sure it is the field and not some value(adding "", example: "title")
        int quotes = 0, count = 0, pos = 0;

        // finding the position when the value starts
        for(int i = 0; i < jsonString.length(); i++)
        {
            if(jsonString.charAt(i) == fixedField.charAt(count))
                count++;
            else
                count = 0;

            if(count == fixedField.length())
            {
                count = 0;

                if(index == 0)
                {
                    pos = i+2; // plus two because i don't want the ending quotes
                    break;
                }

                index--;
            }
        }

        // grabbing the value from the position
        for(int i = pos; i < jsonString.length(); i++)
        {
            // skipping :
            //if(jsonString.charAt(i) == ':')
            //continue;

            // counting quotes
            if(jsonString.charAt(i) == '"')
            {
                quotes++;
                continue;
            }

            // if there is two quotes then i have read all the value content already
            if(quotes == 2)
                break;

            // while we are reading the value(when we are already read the first quote)
            if(quotes == 1)
                value += jsonString.charAt(i);
        }

        return value;
    }

    public boolean isFieldExists(String field)
    {
        String fixedField = "\"" + field + "\""; // making sure it is the field and not some value(adding "", example: "title")
        int count = 0;

        // finding the position when the value starts
        for(int i = 0; i < jsonString.length(); i++)
        {
            if(jsonString.charAt(i) == fixedField.charAt(count))
                count++;
            else
                count = 0;

            if(count == fixedField.length())
                return true;
        }

        return false;
    }
}
