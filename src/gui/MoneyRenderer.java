package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

class MoneyRenderer extends DefaultTableCellRenderer{

    public Component getTableCellRendererComponent(
            JTable table,Object value,boolean isSelected,
            boolean hasFocus,int row,int column){

        JLabel label=(JLabel)super.getTableCellRendererComponent(
                table,value,isSelected,hasFocus,row,column);

        label.setForeground(new Color(30,120,90));
        label.setFont(new Font("Serif",Font.BOLD,15));
        label.setHorizontalAlignment(SwingConstants.RIGHT);

        return label;
    }
}