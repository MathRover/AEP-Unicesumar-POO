# Banco de Alimentos de Londrina

**UniCesumar · Engenharia de Software · 4º semestre**  
**Programação Orientada a Objetos · AEP 2026.2 · Segunda entrega**  
**Integrantes** 
Maria Eduarda Bueno de Azevedo RA: 25177957-2
Matheus Rover da Silva RA: 25032458-2
Samuel de Oliveira Campos RA: 25235501-2 

**Londrina · 2026**

## 1 Objetivo

O MVP registra doadores, recebe doações por lote e distribui alimentos a instituições parceiras. A interface Java Swing consulta o estoque e aplica as regras do domínio ao cadastrar, editar, excluir ou movimentar registros.

## 2 Diagrama de classes e decisões de modelagem

Abra [diagrama_classes.svg](diagrama_classes.svg) no navegador. O diagrama usa como referência as entidades e cardinalidades do modelo de domínio da primeira entrega de Banco de Dados. A descrição abaixo registra a passagem desse modelo para o código Java.

| Decisão | Implementação |
| --- | --- |
| Extrair os dados comuns de contato | `CadastroContato` é abstrata. `Doador` e `Instituicao` herdam os dados comuns e sobrescrevem `documento()`. A representação do contato usa essa operação de forma polimórfica. |
| Representar entregas com múltiplos lotes | `Entrega` compõe uma lista de `LoteEntrega`. Cada item carrega o código do lote e a quantidade expedida. |
| Vincular doações ao estoque | `Doacao` guarda o identificador do doador e os códigos de seus lotes. `Lote` referencia `Alimento`, protege seu saldo e valida o vencimento e o bloqueio na retirada. |
| Preservar as demais associações | `Coleta` referencia uma doação e o serviço limita a uma coleta por doação. `Usuario` referencia `Perfil`. |
| Separar acesso a dados e regras | `Repositorio<K,V>` define o contrato de acesso. `RepositorioEmMemoria` usa `LinkedHashMap`. O serviço aplica validações e lança `RegraNegocioException`. |
| Reutilizar operações de movimentação | O serviço oferece sobrecargas para receber ou entregar um único lote e também listas de lotes. O formulário trabalha com um lote por operação. |

### Organização do código

| Pacote | Responsabilidade |
| --- | --- |
| `aep.dominio` | Entidades, associações, validações e exceção de negócio |
| `aep.persistencia` | Interface e implementação dos repositórios |
| `aep.servico` | CRUD, recebimento, coleta e expedição |
| `aep.ui` | Janela e formulários Swing |

Nesta versão, os dados ficam em memória e a carga de exemplo reaparece a cada abertura. A interface cobre doadores, recebimento, expedição e estoque. Alimentos e instituições vêm da carga de exemplo. Coleta, usuário e perfil fazem parte do modelo Java. A demonstração usa Java 21 e dispensa servidor de banco e bibliotecas externas.

## 3 Como executar

Extraia o ZIP, abra o terminal na pasta extraída e execute com Java 21 ou superior.

```text
java -jar BancoAlimentos.jar
```

Para abrir no IntelliJ, configure o JDK 21, marque `src` como *Sources Root* e execute `aep.ui.Aplicacao`. Para rodar os testes pela IDE, marque `test` como *Test Sources Root* e execute `aep.ui.TesteMvp`.

### Roteiro de demonstração

1. Na aba **Doadores**, cadastre um doador fictício PF com nome, CPF de 11 dígitos e e-mail válido. Selecione a linha para consultar e alterar o nome. Exclua o registro antes de associá-lo a uma doação.
2. Na aba **Estoque e movimentações**, registre um recebimento para **Mercado Exemplo**. Escolha arroz, informe um código novo e receba 10 kg com a validade sugerida. O lote deve aparecer com saldo de 10 kg.
3. Escolha o novo lote e registre uma entrega de 3 kg. Confira o saldo de 7 kg. Em seguida, tente retirar 8 kg. O saldo permanece em 7 kg.
4. Tente entregar 1 L do lote `LET-001`, que já está vencido na carga inicial. A operação é rejeitada.

## 4 Testes executados

| Cenário | Resultado conferido |
| --- | --- |
| Cadastro, consulta, edição e exclusão pela interface | A tabela e os registros do serviço refletem as ações |
| Campos obrigatórios vazios e CPF/CNPJ duplicado | Operação rejeitada |
| Exclusão de doador vinculado a uma doação | Operação rejeitada |
| Recebimento e entrega válidos pela interface | Saldo de 10,5 kg reduzido para 7,5 kg após entrega de 3 kg |
| Estoque insuficiente | Nenhuma entrega adicional registrada |
| Validade anterior ao recebimento e segunda coleta da mesma doação | Operações rejeitadas |
| Entrega com lote vencido ou bloqueado | Operação rejeitada |
| Entrega com item válido seguido de item vencido | A entrega inteira é rejeitada e o saldo do item válido permanece intacto |

Os testes automatizados acionam os botões e campos dos painéis Swing em modo sem janela e verificam os resultados do serviço.
