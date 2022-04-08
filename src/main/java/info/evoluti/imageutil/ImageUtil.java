package info.evoluti.imageutil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Rodrigo
 */
public class ImageUtil {

    private final DecimalFormat DF = new DecimalFormat("#,##0.00");

    private final Dimension TAMANHO_MAX = new Dimension(500, 500);

    private int reduzirQualidade = 50;

    private Dimension dimensao;

    private File imagem;

    private String nomeImagem;
    private String pastaImagem;
    private String extensaoImagem;
    private String pastaParaSalvar;

    private byte[] imageBytes;

    private boolean gZipUtilizado = false;

    public static ImageUtil init() {
        ImageUtil iu = new ImageUtil();
        return iu;
    }

    public static ImageUtil init(File imagem) throws Exception {
        ImageUtil iu = new ImageUtil();
        iu.setImagem(imagem);
        return iu;
    }

    public ImageUtil() {
    }

    public ImageUtil setDimensao(Dimension dimensao) {
        this.dimensao = dimensao;
        return this;
    }

    public ImageUtil setReduzirQualidade(int reduzirQualidade) {
        this.reduzirQualidade = reduzirQualidade;
        return this;
    }

    public ImageUtil setPastaParaSalvar(String pastaParaSalvar) {
        this.pastaParaSalvar = pastaParaSalvar;
        return this;
    }

    public File getImagem() {
        return imagem;
    }

    public String getNomeImagem() {
        return nomeImagem;
    }

    public String getPastaImagem() {
        return pastaImagem;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    /**
     * Ao informar a imagem esse metodo já irá criar o imageBytes dentro da,
     * classe e tambem realizará as validações necessárias
     *
     * @param imagem
     * @return
     * @throws Exception
     */
    public ImageUtil setImagem(File imagem) throws Exception {

        if (imagem == null || !imagem.exists()) {
            throw new FileNotFoundException("Arquivo inválido e/ou não encontrado!");
        }

        this.nomeImagem = imagem.getName();
        this.pastaImagem = imagem.getParent();
        this.extensaoImagem = imagem.getPath().substring(imagem.getPath().lastIndexOf(".") + 1);

        if (!"jpg".equals(this.extensaoImagem.toLowerCase())) {
            throw new Exception("Arquivo inválido, somente é permitido arquivo com extensão \".jpg\"!");
        }

        this.imagem = imagem;

        byte[] bytes = new byte[(int) imagem.length()];
        try (FileInputStream fis = new FileInputStream(imagem)) {
            fis.read(bytes);
        }
        this.imageBytes = bytes;

        return this;
    }

    /**
     * Comprimir qualidade da imagem informando direto no metodo a qualidade a
     * ser reduzida
     *
     * @param percentualReduzirQualidade
     * @return
     * @throws IOException
     */
    public ImageUtil comprimirQualidade(int percentualReduzirQualidade) throws IOException {
        this.reduzirQualidade = percentualReduzirQualidade;
        return comprimirQualidade();
    }

    /**
     * Comprimir qualidade da imagem com valor padrão para redução de 15%
     *
     * @return
     * @throws IOException
     */
    public ImageUtil comprimirQualidade() throws IOException {

        ImageWriter writer = null;

        BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(this.imageBytes));

        // Get image type
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");

        if (!iter.hasNext()) {
            throw new IOException("O tipo de imagem não é reconhecível");
        }

        writer = (ImageWriter) iter.next();

        ImageWriteParam iwp = writer.getDefaultWriteParam();

        float compressionSize = (float) (this.reduzirQualidade / 100.0);

        try {
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(compressionSize);
        } catch (UnsupportedOperationException e) {
            throw new UnsupportedOperationException(e);
        }

        IIOImage iioiImage = new IIOImage(inputImage, null, null);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        ImageOutputStream imgOutStrm = ImageIO.createImageOutputStream(buffer);

        writer.setOutput(imgOutStrm);
        writer.write(null, iioiImage, iwp);
        imgOutStrm.close();
        writer.dispose();

        this.imageBytes = buffer.toByteArray();

        return this;
    }

    /**
     * Comprimir tamanho da imagem informando um 'Dimension' com altura e
     * largura, somente será dimensionado até o máximo de uma das medidas para
     * manter o aspecto da imagem
     *
     * @param dimensao
     * @return
     * @throws IOException
     */
    public ImageUtil comprimirTamanho(Dimension dimensao) throws IOException {
        this.dimensao = dimensao;
        return comprimirTamanho();
    }

    /**
     * Comprimir tamanho da imagem com valor padrão de dimensão, somente será
     * dimensionado até o máximo de uma das medidas para manter o aspecto da
     * imagem
     *
     * @return
     * @throws IOException
     */
    public ImageUtil comprimirTamanho() throws IOException {

        if (this.dimensao == null) {
            this.dimensao = TAMANHO_MAX;
        }

        ByteArrayInputStream in = new ByteArrayInputStream(this.imageBytes);

        BufferedImage img = ImageIO.read(in);

        Dimension aspectRatio = getScaledDimension(new Dimension(img.getWidth(), img.getHeight()), this.dimensao);

        Image scaledImage = img.getScaledInstance(aspectRatio.width, aspectRatio.height, Image.SCALE_SMOOTH);
        BufferedImage imageBuff = new BufferedImage(aspectRatio.width, aspectRatio.height, BufferedImage.TYPE_INT_RGB);
        imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0, 0, 0), null);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        ImageIO.write(imageBuff, "jpg", buffer);

        this.imageBytes = buffer.toByteArray();

        return this;
    }

    /**
     * Dimensionar a imagem mantendo seu aspecto, sem distorcer
     *
     * @param imgTamanho
     * @param tamanhoMaximo
     * @return
     */
    private Dimension getScaledDimension(Dimension imgTamanho, Dimension tamanhoMaximo) {
        int widthOriginal = imgTamanho.width;
        int heightOriginal = imgTamanho.height;
        int widthMax = tamanhoMaximo.width;
        int heightMax = tamanhoMaximo.height;
        int widthFinal = widthOriginal;
        int heightFinal = heightOriginal;

        // first check if we need to scale width
        if (widthOriginal > widthMax) {
            //scale width to fit
            widthFinal = widthMax;
            //scale height to maintain aspect ratio
            heightFinal = (widthFinal * heightOriginal) / widthOriginal;
        }

        // then check if we need to scale even with the new height
        if (heightFinal > heightMax) {
            //scale height to fit instead
            heightFinal = heightMax;
            //scale width to maintain aspect ratio
            widthFinal = (heightFinal * widthOriginal) / heightOriginal;
        }

        return new Dimension(widthFinal, heightFinal);
    }

    /**
     * Transforma o imageBytes em ImageIcon
     *
     * @return
     */
    public ImageIcon getImageIcon() {
        ImageIcon img = null;
        if (this.imageBytes != null) {
            BufferedImage image = null;
            try {
                image = ImageIO.read(new ByteArrayInputStream(this.imageBytes));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Problemas ao ler bytes da imagem informada!\n\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
            img = new ImageIcon(image);
        }
        return img;
    }

    /**
     * Salva imagem local no computador
     *
     * @throws IOException
     */
    public String salvarImagem() throws IOException {
        String caminhoDaImagem = "";

        byte[] imagemSalvar = this.imageBytes;

        if (this.gZipUtilizado) {
            imagemSalvar = gUnZip(imagemSalvar);
        }

        if (this.pastaParaSalvar == null) {
            this.pastaParaSalvar = this.pastaImagem;
        }

        caminhoDaImagem = this.pastaParaSalvar + File.separator + this.nomeImagem;

        if (new File(caminhoDaImagem).exists()) {
            caminhoDaImagem = this.pastaParaSalvar + File.separator + "COMPRIMIDA-" + this.nomeImagem;
        }

        writeBytesToFile(caminhoDaImagem, imagemSalvar);

        return caminhoDaImagem;
    }

    /**
     * Utiliza o imageButes para salvar em um arquivo (File)
     *
     * @param fileOutput
     * @param bytes
     * @throws IOException
     */
    private void writeBytesToFile(String fileOutput, byte[] bytes)
            throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileOutput)) {
            fos.write(bytes);
        }
    }

    /**
     * Comprimir o imageBytes com GZip
     *
     * @param data
     * @return
     * @throws IOException
     */
    public ImageUtil gZip() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(this.imageBytes.length)) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(this.imageBytes);
            }
            this.imageBytes = bos.toByteArray();
        }
        return this;
    }

    /**
     * Extrair o imageBytes GZip
     *
     * @param zip
     * @return
     * @throws IOException
     */
    public byte[] gUnZip(byte[] zip) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(zip);

        byte[] readBuffer = new byte[5000];
        GZIPInputStream inputStream = new GZIPInputStream(bis);
        int read = inputStream.read(readBuffer, 0, readBuffer.length);
        inputStream.close();
        // Should hold the original (reconstructed) data
        return Arrays.copyOf(readBuffer, read);
    }

    /**
     * Extrair o imageBytes do GZip
     *
     * @param zip
     * @return
     * @throws IOException
     */
    public ImageUtil gUnZip() throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(this.imageBytes);

        byte[] readBuffer = new byte[5000];
        GZIPInputStream inputStream = new GZIPInputStream(bis);
        int read = inputStream.read(readBuffer, 0, readBuffer.length);
        inputStream.close();
        // Should hold the original (reconstructed) data
        this.imageBytes = Arrays.copyOf(readBuffer, read);
        return this;
    }

    /**
     * Retorna o peso da imagem em KB (BigDecimal)
     *
     * @return
     */
    public String getTamanho() {
        BigDecimal _Kb = new BigDecimal(1024);
        BigDecimal _Mb = _Kb.multiply(_Kb);
        BigDecimal _Gb = _Mb.multiply(_Kb);
        BigDecimal _Tb = _Gb.multiply(_Kb);

        BigDecimal kbImagem = new BigDecimal(this.imageBytes.length);

        if (kbImagem.compareTo(_Mb) == -1) {
            kbImagem = kbImagem.divide(_Kb, 2, RoundingMode.DOWN);
            return DF.format(kbImagem) + " KB";
        } else if (kbImagem.compareTo(_Gb) == -1) {
            kbImagem = kbImagem.divide(_Mb, 2, RoundingMode.DOWN);
            return DF.format(kbImagem) + " MB";
        } else if (kbImagem.compareTo(_Tb) == -1) {
            kbImagem = kbImagem.divide(_Gb, 2, RoundingMode.DOWN);
            return DF.format(kbImagem) + " GB";
        } else {
            return DF.format(kbImagem) + " NONE";
        }
    }

    /**
     * Validar o tamanho da imagem para ver se precisa ser comprimida para
     * salvar dentro do banco de dados
     *
     * @return
     */
    public boolean validarTamanho() {
        String tamanho = getTamanho();

        if (tamanho.contains("KB")) {
            BigDecimal tamanhoMaximo = new BigDecimal("64");
            BigDecimal validar = new BigDecimal(tamanho.replace(",", ".").replace(" KB", ""));

            return validar.compareTo(tamanhoMaximo) == 1;
        } else {
            return true;
        }
    }

    /**
     * Neste metodo será testado se o tamanho do arquivo já alçancou o limite
     * que precisava para salvar no banco, se sim é parado a compressão de
     * outros metodos e salvo uma imagem no lugar de origem
     *
     * @throws Exception
     */
    public void compressaoEmFila() throws Exception {
        boolean isPrecisaComprimir = validarTamanho();

        if (isPrecisaComprimir) {
            comprimirQualidade();
        }

        isPrecisaComprimir = validarTamanho();

        if (isPrecisaComprimir) {
            comprimirTamanho();
        }

        isPrecisaComprimir = validarTamanho();

        if (isPrecisaComprimir) {
            gZip();
        }

        salvarImagem();
    }

    public static void main(String[] args) throws Exception {

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        int retorno = jfc.showOpenDialog(null);

        if (retorno == JFileChooser.APPROVE_OPTION) {

            ImageUtil iu = ImageUtil.init(jfc.getSelectedFile());

            // Comprimir qualidade com valor padrão de 50%
            iu.comprimirQualidade();

            // Comprimir qualidade com valor informado de 40%
            iu.comprimirQualidade(45);

            // Comprimir tamanho com valor padrão de 500 x 500
            iu.comprimirTamanho();

            // Comprimir tamanho com valor informado de 800 x 800
            iu.comprimirTamanho(new Dimension(800, 800));
            // Obs.: Somente será comprimido no máximo de cada altura e largura
            // para manter o aspecto da imagem 100%

            // GZIP para salvar a imagem em byte array no banco de dados
            iu.gZip();

            // Obtem o byte array para salvar no banco de dados
            byte[] bytes = iu.getImageBytes();

            // Informar pasta para o local que será salvo
            iu.setPastaParaSalvar("D:\\pasta");

            // Salvar arquivo compresso
            iu.salvarImagem();
            // Obs.: Se caso for na mesma pasta de origem o arquivo terá um prefixo
            // com nome "COMPRIMIDO-..."
            
            // Comprimir imagem até ela chegar abaixo de 64KB que é o campo do MySQL
            // 'BLOB' para ser salvo, se precisr alterar isso deve ser feito no código fonte
            iu.compressaoEmFila();
        }

    }
}
