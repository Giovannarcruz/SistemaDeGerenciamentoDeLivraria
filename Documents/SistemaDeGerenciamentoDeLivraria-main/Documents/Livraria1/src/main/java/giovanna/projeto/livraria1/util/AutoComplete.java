package giovanna.projeto.livraria1.util;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Classe para adicionar funcionalidade de autocompletar em um JTextField.
 *
 * @param <O> Tipo genérico dos itens utilizados para autocompletar.
 */
public class AutoComplete<O> {

    /**
     * Configura o campo de texto para autocompletar com base na lista
     * fornecida.
     *
     * @param <O> Tipo genérico dos itens utilizados para autocompletar.
     * @param textField Campo de texto.
     * @param popupMenu Menu suspenso que exibe sugestões.
     * @param items Lista de itens de sugestões.
     * @param toStringFunction Função para converter os itens em texto.
     */
    public static <O> void configureAutoComplete(
            JTextField textField,
            JPopupMenu popupMenu,
            List<O> items,
            java.util.function.Function<O, String> toStringFunction) {

        final int[] selectedIndex = {-1};

        // Listener para atualizar sugestões dinamicamente
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateMenu();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateMenu();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Não usado para mudanças estilísticas.
            }

            private void updateMenu() {
                SwingUtilities.invokeLater(() -> {
                    String text = textField.getText().toLowerCase().trim();
                    if (text.isEmpty()) {
                        popupMenu.setVisible(false);
                        return;
                    }

                    // Filtra itens baseados no texto
                    List<String> filteredItems = items.stream()
                            .map(toStringFunction)
                            .filter(s -> s != null && s.toLowerCase().startsWith(text))
                            .limit(10)
                            .collect(Collectors.toList());

                    if (filteredItems.isEmpty()) {
                        popupMenu.setVisible(false);
                        return;
                    }

                    // Atualiza o menu suspenso
                    popupMenu.setVisible(false); // Fecha o menu para recriá-lo
                    populatePopupMenu(popupMenu, filteredItems, textField);
                    popupMenu.show(textField, 0, textField.getHeight());
                    selectedIndex[0] = -1; // Reseta o índice ao atualizar
                });
            }
        });

        // Configura navegação no menu por teclado
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!popupMenu.isVisible() || popupMenu.getComponentCount() == 0) {
                    return;
                }

                int itemCount = popupMenu.getComponentCount();

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    selectedIndex[0] = (selectedIndex[0] + 1) % itemCount;
                    highlightMenuItem(popupMenu, selectedIndex[0]);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    selectedIndex[0] = (selectedIndex[0] - 1 + itemCount) % itemCount;
                    highlightMenuItem(popupMenu, selectedIndex[0]);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && selectedIndex[0] >= 0) {
                    JMenuItem menuItem = (JMenuItem) popupMenu.getComponent(selectedIndex[0]);
                    menuItem.doClick();
                    e.consume();
                }
            }
        });
        // Garante que o foco permanece no campo de texto
        popupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                SwingUtilities.invokeLater(textField::requestFocusInWindow);
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                selectedIndex[0] = -1; // Reinicia o índice quando o menu é fechado
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                selectedIndex[0] = -1; // Reinicia o índice se o menu for cancelado
            }
        });
    }

    /**
     * Preenche o menu suspenso com os itens fornecidos.
     */
    private static void populatePopupMenu(JPopupMenu popupMenu, List<String> items, JTextField textField) {
        popupMenu.removeAll();
        items.forEach(item -> {
            JMenuItem menuItem = new JMenuItem(item);
            menuItem.addActionListener(e -> {
                textField.setText(item);
                textField.setCaretPosition(item.length());
                popupMenu.setVisible(false);
            });
            popupMenu.add(menuItem);
        });
    }

    /**
     * Realça o item selecionado no menu.
     */
    private static void highlightMenuItem(JPopupMenu popupMenu, int index) {
        for (int i = 0; i < popupMenu.getComponentCount(); i++) {
            popupMenu.getComponent(i).setBackground(null);
        }
        if (index >= 0) {
            popupMenu.getComponent(index).setBackground(java.awt.Color.LIGHT_GRAY);
        }
    }
}
