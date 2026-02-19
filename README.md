# 📚 Gerenciador e Leitor de Livros (Java)

Um aplicativo desktop moderno desenvolvido em Java para gerenciar sua biblioteca pessoal de PDFs e oferecer uma experiência de leitura avançada, confortável e interativa.

## ✨ Funcionalidades

### 📖 Gerenciamento
* **Cadastro de Livros:** Adicione e organize seus livros em formato PDF.
* **Persistência de Dados:** Todo o progresso de leitura, favoritos e grifos são salvos automaticamente em formato `JSON`. O aplicativo sempre lembra onde você parou!

### 🔍 Leitor de PDF Embutido
* **Visualização Flexível:** Alterne entre visualização de página única ou página dupla (estilo livro aberto).
* **Modo Noturno (Dark Mode):** Inversão inteligente de cores do PDF para leituras confortáveis em ambientes escuros, com otimização nativa de performance.
* **Zoom Interativo:** Controle total sobre o tamanho da página usando atalhos de teclado ou a rodinha do mouse, com renderização de alta qualidade (Graphics2D Bilinear).
* **Sistema de Grifos (Marca-texto):** Selecione e grife partes importantes da página. Os grifos são calculados de forma proporcional, mantendo o alinhamento perfeito mesmo quando você altera o zoom.
* **Marcadores / Favoritos:** Salve páginas importantes e acesse-as rapidamente através de um painel lateral retrátil.

## 🛠️ Tecnologias Utilizadas

* **Java (Swing):** Interface gráfica nativa do projeto.
* **[Apache PDFBox](https://pdfbox.apache.org/):** Motor robusto para carregamento e renderização das páginas do PDF.
* **[FlatLaf](https://www.formdev.com/flatlaf/):** Look and Feel moderno que traz um design limpo e renderização de ícones em SVG.
* **JSON (Jackson/Gson):** Estruturação e salvamento dos dados do usuário (`lendo.json`).

## ⌨️ Atalhos de Teclado (Leitor)

Para uma navegação mais fluida, o leitor possui os seguintes atalhos:

| Ação | Atalho |
| :--- | :--- |
| **Próxima Página** | `Seta para Direita` |
| **Página Anterior** | `Seta para Esquerda` |
| **Rolar para Cima/Baixo** | `Seta Cima` / `Seta Baixo` |
| **Ativar/Desativar Grifo** | `CTRL + M` (ou segure `SHIFT` e arraste) |
| **Favoritar Página Atual** | `CTRL + D` |
| **Zoom In (+)** | `CTRL + Rodinha do Mouse p/ cima` ou `CTRL +` |
| **Zoom Out (-)** | `CTRL + Rodinha do Mouse p/ baixo` ou `CTRL -` |
| **Resetar Zoom (100%)** | `CTRL + 0` |

## 🚀 Como Executar o Projeto

1. Certifique-se de ter o **Java JDK** instalado em sua máquina.
2. Clone este repositório:
   ```bash
   git clone [https://github.com/SeuUsuario/Gerenciador_De_Livros.git](https://github.com/SeuUsuario/Gerenciador_De_Livros.git)

Tela: Gerenciador da interface gráfica e eventos de usuário.


<img width="1366" height="767" alt="image" src="https://github.com/user-attachments/assets/f820bac4-9813-458e-83ad-670f7466d9f6" />
<img width="783" height="762" alt="image" src="https://github.com/user-attachments/assets/f2fa9f4a-9ec7-4d81-8498-ef6eb03e74c9" />
<img width="488" height="757" alt="image" src="https://github.com/user-attachments/assets/8fe9310d-9a2e-4eeb-b758-0221a46c65f1" />
<img width="870" height="762" alt="image" src="https://github.com/user-attachments/assets/4227cc50-d789-47d1-a627-06ccbd0ffd93" />




