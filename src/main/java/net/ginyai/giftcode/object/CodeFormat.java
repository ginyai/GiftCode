package net.ginyai.giftcode.object;

import net.ginyai.giftcode.GiftCodePlugin;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeFormat {

    private static final Pattern PATTERN = Pattern.compile("\\{(.*?):([0-9]+)}");

    private String formatString;
    private List<?> format;

    public CodeFormat(String formatString){
        this.formatString = formatString;
//        Matcher matcher = PATTERN.matcher(formatString);
//        if(matcher.find()){
//            matcher.
//        }
    }

    public String getFormatString() {
        return formatString;
    }

    public String genCode(){
        String out = formatString;
        Matcher matcher = PATTERN.matcher(out);
        while (matcher.find()){
            String charSet = GiftCodePlugin.getInstance().getConfig().getCharSet(matcher.group(1));
            int length = Integer.parseInt(matcher.group(2));
            String replacement = new RandomPart(charSet,length).gen();
            out = matcher.replaceFirst(replacement);
            matcher = PATTERN.matcher(out);
        }
        return out;
//        StringBuilder builder = new StringBuilder();
//        for(Object o:format){
//            if(o instanceof RandomPart){
//                builder.append(((RandomPart) o).gen());
//            }else {
//                builder.append(o.toString());
//            }
//        }
//        return builder.toString();
    }

    private static class RandomPart{
        private String charSet;
        private int length;

        private RandomPart(String charSet, int length) {
            this.charSet = charSet;
            this.length = length;
        }

        public String gen(){
            StringBuilder out = new StringBuilder();
            for(int i=0;i<length;i++){
                out.append(charSet.charAt((int) (Math.random()*charSet.length())));
            }
            return out.toString();
        }
    }
}
