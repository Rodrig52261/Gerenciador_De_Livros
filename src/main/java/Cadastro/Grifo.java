package Cadastro;

public class Grifo {
    private int pagina;
    private double xRatio; // Posição X em porcentagem (0.0 a 1.0)
    private double yRatio; // Posição Y em porcentagem
    private double wRatio; // Largura em porcentagem
    private double hRatio; // Altura em porcentagem

    public Grifo() {} // Necessário para o JSON

    public Grifo(int pagina, double xRatio, double yRatio, double wRatio, double hRatio) {
        this.pagina = pagina;
        this.xRatio = xRatio;
        this.yRatio = yRatio;
        this.wRatio = wRatio;
        this.hRatio = hRatio;
    }

    // Crie os Getters e Setters aqui...
    public int getPagina() { return pagina; }
    public double getxRatio() { return xRatio; }
    public double getyRatio() { return yRatio; }
    public double getwRatio() { return wRatio; }
    public double gethRatio() { return hRatio; }
}