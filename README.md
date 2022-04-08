
# ImageUtil

Este código foi desenvolvido para tratar imagens que sejam muito pesadas, assim ao salvar no banco de dados MySQL/PostgreSQL/etc... não fiquem tão pesadas no banco de dados, para testar no seu computador pode ser baixado o '.jar' que tem nas versões do repostório e se caso quiser utilizar em seu projeto recomendo somente copiar a classe 'ImageUtil.java' que tem dentro deste repositório. A baixo segue um uso básico da classe :D

#### 'somente é permitido imagens com extensão .jpg'

---
* Teste rápido
```java

JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
int retorno = jfc.showOpenDialog(null);

if (retorno == JFileChooser.APPROVE_OPTION) {
    ImageUtil iu = ImageUtil.init(jfc.getSelectedFile());
    iu.compressaoEmFila();
}

```

* Todas funções
```java

File imagem = new File("teste.jpg");

ImageUtil iu = ImageUtil.init(imagem ));

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


```
