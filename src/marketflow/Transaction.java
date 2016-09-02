package marketflow;

public class Transaction
{
    private String Resource;
    private int Price;
    public Transaction(String rid, int price)
    {
        Resource=rid;
        Price=price;
    }
    public String Resource()
    {
        return Resource;
    }
    public int Price()
    {
        return Price;
    }
}
