package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

class StatusRenderer extends DefaultTableCellRenderer{

    public Component getTableCellRendererComponent(
            JTable table,Object value,boolean isSelected,
            boolean hasFocus,int row,int column){

        JLabel label=(JLabel)super.getTableCellRendererComponent(
                table,value,isSelected,hasFocus,row,column);

        label.setHorizontalAlignment(SwingConstants.CENTER);

        if(value.equals("Confirmed")){
            label.setBackground(new Color(210,230,210));
            label.setForeground(new Color(30,100,60));
        }
        else if(value.equals("Checked In")){
            label.setBackground(new Color(210,220,240));
            label.setForeground(new Color(50,80,140));
        }
        else{
            label.setBackground(new Color(240,220,220));
            label.setForeground(new Color(140,60,60));
        }

        label.setOpaque(true);

        return label;
    }
}