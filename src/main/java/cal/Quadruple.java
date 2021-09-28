package cal;

public class Quadruple {
    private final String op;
    private final String arg1;
    private final String arg2;
    private final String result;

    public Quadruple(String op, String arg1, String arg2, String result) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    public Quadruple(String op, String arg1, String result) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = (op.equals("-")) ? "0" : null;
        this.result = result;
    }

    public String toIRCode() {
        if (this.op.equals("="))
            return String.format("\t%s %s %s", this.result, this.op, this.arg1);

        if (arg2 != null)
            return String.format("\t%s = %s %s %s", this.result, this.arg2, this.op, this.arg1);
        
        return String.format("\t%s = %s %s", this.result, this.op, this.arg1);
    }

    @Override
    public String toString() {
        return "Quadruple{" +
                "op='" + op + '\'' +
                ", arg1='" + arg1 + '\'' +
                ", arg2='" + arg2 + '\'' +
                ", result='" + result + '\'' +
                '}';
    }

    public static void main(String[] args) {
        Quadruple quad1 = new Quadruple("minus", "a", "b", "x");
        Quadruple quad2 = new Quadruple("minus", "a", "x");
        System.out.println(quad1.toIRCode());
        System.out.println(quad2.toIRCode());
    }
}
