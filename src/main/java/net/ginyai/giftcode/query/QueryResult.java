package net.ginyai.giftcode.query;

import net.ginyai.giftcode.object.GiftCode;

public class QueryResult {
    private final Query query;
    private final GiftCode giftCode;
    private final Result result;

    public QueryResult(Query query, GiftCode giftCode, Result result) {
        this.query = query;
        this.giftCode = giftCode;
        this.result = result;
    }

    public Query getQuery() {
        return query;
    }

    public GiftCode getGiftCode() {
        return giftCode;
    }

    public Result getResult() {
        return result;
    }

    public enum Result{
        SUCCESS,
        NOT_FOUND,
        USED,
        ERROR
    }
}
