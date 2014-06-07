package net.hearthstats.ui

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform
import java.awt.geom.NoninvertibleTransformException
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.SwingUtilities
import net.hearthstats.Card
import ClickableLabel._
import scala.collection.JavaConversions._
import scala.swing.Swing._
import javax.swing.BorderFactory

class ClickableLabel(var card: Card) extends JLabel {

  var remaining = card.count
  private var cardImage = new ImageIcon(card.localURL)
  private var currentBack = cardBack
  private var cost = card.cost.toString
  private var name = card.name
  private val imgDstX = 100
  private val imgDstY = 0
  private val imgDstW = 113
  private val imgDstH = 35
  private val imgSrcX = 81 * cardImage.getIconWidth() / 289
  private val imgSrcY = 62 * cardImage.getIconHeight() / 398
  private val imgSrcW = 130 * cardImage.getIconWidth() / 289
  private val imgSrcH = 40 * cardImage.getIconHeight() / 398

  val displaySize = new Dimension(currentBack.getIconWidth(), currentBack.getIconHeight())

  setPreferredSize(displaySize)
  setMaximumSize(displaySize)
  setMinimumSize(displaySize)

  setBorder(BorderFactory.createEmptyBorder)

  updateRemaining()

  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) {
      onEDT(handleClick(e.getButton))
    }
  })

  protected override def paintComponent(g: Graphics) {
    val g2 = g.asInstanceOf[Graphics2D]
    val originalTrans = g2.getTransform()
    val composite = g2.getComposite

    if (remaining < 1) {
      g2.setColor(Color.BLACK)
      g2.fillRoundRect(0, 0, currentBack.getIconWidth(), currentBack.getIconHeight(), 15, 15)
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f))
    }
    g2.drawImage(cardImage.getImage, imgDstX, imgDstY, imgDstX + imgDstW, imgDstY + imgDstH, imgSrcX, imgSrcY, imgSrcX + imgSrcW, imgSrcY + imgSrcH, null)
    g2.drawImage(currentBack.getImage, 0, 0, null)
    g2.setFont(Font.decode(Font.SANS_SERIF).deriveFont(Font.BOLD, 18))
    if (card.cost < 10)
      outlineText(g2, cost, 9, 25, Color.BLACK, Color.WHITE)
    else
      outlineText(g2, cost, 4, 25, Color.BLACK, Color.WHITE)
    g2.setFont(Font.decode(Font.SANS_SERIF).deriveFont(Font.BOLD, 12))
    outlineText(g2, name, 35, 23, Color.BLACK, Color.WHITE)
    g2.setComposite(composite)

    super.paintComponent(g2)
  }

  private def outlineText(g: Graphics, s: String, posX: Int, posY: Int, borderColor: Color, fontColor: Color): Unit = {
    g.setColor(borderColor)
    g.drawString(s, posX - 1, posY - 1)
    g.drawString(s, posX - 1, posY + 1)
    g.drawString(s, posX + 1, posY - 1)
    g.drawString(s, posX + 1, posY + 1)
    g.setColor(fontColor)
    g.drawString(s, posX, posY)
  }

  private def handleClick(button: Int): Unit =
    if (button == MouseEvent.BUTTON1 && remaining > 0)
      decreaseRemaining()
    else if (button != MouseEvent.BUTTON1 && remaining < card.count)
      increaseRemaining()

  def decreaseRemaining(): Unit = {
    remaining -= 1
    updateRemaining()
  }

  def increaseRemaining(): Unit = {
    remaining += 1
    updateRemaining()
  }

  private def updateRemaining() {
    currentBack =
      if (remaining > 1) cardBack2
      else if (card.isLegendary && remaining >= 1) cardBackL
      else cardBack
    repaint()
  }
}

object ClickableLabel {

  val Seq(cardBack, cardBack2, cardBackL) =
    Seq("cardBack", "cardBack2", "cardBackL") map buildImage

  def buildImage(n: String) =
    new ImageIcon(classOf[ClickableLabel].getResource(s"/images/$n.png"))
}