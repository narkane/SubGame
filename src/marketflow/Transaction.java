package marketflow;

public class Transaction
{
    int ID;
    private String Resource;
    private int Price;
    public Transaction(int id, String rid, int price)
    {
        ID=id;
        Resource=rid;
        Price=price;
    }
    public int ID() { return ID; }
    public String Resource()
    {
        return Resource;
    }
    public int Price() { return Price;}
}
