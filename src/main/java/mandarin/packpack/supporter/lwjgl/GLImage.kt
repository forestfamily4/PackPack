package mandarin.packpack.supporter.lwjgl

import common.system.fake.FakeGraphics
import common.system.fake.FakeImage
import mandarin.packpack.supporter.StaticStore
import mandarin.packpack.supporter.lwjgl.opengl.model.SpriteSheet
import mandarin.packpack.supporter.lwjgl.opengl.model.TextureMesh
import org.lwjgl.opengl.GL33
import java.awt.Color
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class GLImage : FakeImage {
    companion object {
        private val sharedRGBA = IntArray(4)
    }

    private val cloneReferences = ArrayList<GLImage>()

    private var sprite: SpriteSheet
    private var textureMesh: TextureMesh
    private var reference: GLImage?

    constructor(spriteSheet: SpriteSheet) {
        sprite = spriteSheet
        textureMesh = sprite.wholePart
        reference = null
    }

    constructor(spriteSheet: SpriteSheet, child: TextureMesh) {
        sprite = spriteSheet
        textureMesh = child
        reference = null
    }

    private constructor(spriteSheet: SpriteSheet, child: TextureMesh, cloned: GLImage?) {
        sprite = spriteSheet
        textureMesh = child
        this.reference = cloned
    }

    override fun bimg(): Any {
        return textureMesh
    }

    override fun getHeight(): Int {
        return textureMesh.height.toInt()
    }

    override fun getRGB(i: Int, j: Int): Int {


        val waiter = if (!Thread.currentThread().equals(StaticStore.renderManager.renderThread)) {
            CountDownLatch(1)
        } else {
            null
        }

        return if (waiter != null) {
            val rgb = AtomicReference<Int>(null)

            StaticStore.renderManager.queueGL {
                sprite.bind()

                GL33.glReadPixels(i, j, 1, 1, GL33.GL_FLOAT, GL33.GL_UNSIGNED_INT, sharedRGBA)

                rgb.set(Color(sharedRGBA[0], sharedRGBA[1], sharedRGBA[2]).rgb)

                waiter.countDown()
            }

            waiter.await()

            rgb.get()
        } else {
            sprite.bind()

            GL33.glReadPixels(i, j, 1, 1, GL33.GL_FLOAT, GL33.GL_UNSIGNED_INT, sharedRGBA)

            Color(sharedRGBA[0], sharedRGBA[1], sharedRGBA[2]).rgb
        }


    }

    override fun getSubimage(i: Int, j: Int, k: Int, l: Int): FakeImage {
        val waiter = if (!Thread.currentThread().equals(StaticStore.renderManager.renderThread)) {
            CountDownLatch(1)
        } else {
            null
        }

        return if (waiter != null) {
            val image = AtomicReference<GLImage>(null)

            StaticStore.renderManager.queueGL {
                image.set(GLImage(sprite, sprite.generatePart(i.toFloat(), j.toFloat(), k.toFloat(), l.toFloat())))

                waiter.countDown()
            }

            waiter.await()

            image.get()
        } else {
            GLImage(sprite, sprite.generatePart(i.toFloat(), j.toFloat(), k.toFloat(), l.toFloat()))
        }

    }

    override fun getWidth(): Int {
        return textureMesh.width.toInt()
    }

    override fun gl(): Any? {
        return null
    }

    override fun isValid(): Boolean {
        return !sprite.released
    }

    override fun setRGB(i: Int, j: Int, p: Int) {
        StaticStore.renderManager.queueGL {
            sprite.bind()

            val c = Color(p)

            sharedRGBA[0] = c.red
            sharedRGBA[1] = c.green
            sharedRGBA[2] = c.blue
            sharedRGBA[3] = c.alpha

            GL33.glTexSubImage2D(GL33.GL_TEXTURE_2D, 0, i, j, 1, 1, GL33.GL_RGBA, GL33.GL_UNSIGNED_INT, sharedRGBA)
        }
    }

    override fun unload() {
        if (reference != null)
            return

        if (!Thread.currentThread().equals(StaticStore.renderManager.renderThread)) {
            val waiter = CountDownLatch(1)

            StaticStore.renderManager.queueGL {
                if (cloneReferences.isNotEmpty()) {
                    cloneReferences.forEach { ref ->
                        ref.changeReferenceTo(hardClone())
                    }
                }

                sprite.release()

                waiter.countDown()
            }

            waiter.await()
        } else {
            if (cloneReferences.isNotEmpty()) {
                cloneReferences.forEach { ref ->
                    ref.changeReferenceTo(hardClone())
                }
            }

            sprite.release()
        }
    }

    override fun cloneImage(): FakeImage {
        val img = if (reference != null)
            GLImage(sprite, textureMesh, reference)
        else
            GLImage(sprite, textureMesh, this)

        if (reference != null)
            reference?.cloneReferences?.add(img)
        else
            cloneReferences.add(img)

        return img
    }

    private fun hardClone() : GLImage {
        val waiter = if (!Thread.currentThread().equals(StaticStore.renderManager.renderThread)) {
            CountDownLatch(1)
        } else {
            null
        }

        return if (waiter != null) {
            val img = AtomicReference<GLImage>(null)

            StaticStore.renderManager.queueGL {
                if (textureMesh === sprite.wholePart) {
                    img.set(GLImage(sprite.clone()))
                } else {
                    img.set(GLImage(sprite.clone()).getSubimage(textureMesh.offsetX.toInt(), textureMesh.offsetY.toInt(), textureMesh.width.toInt(), textureMesh.height.toInt()) as GLImage)
                }

                waiter.countDown()
            }

            waiter.await()

            img.get()
        } else {
            if (textureMesh === sprite.wholePart) {
                GLImage(sprite.clone())
            } else {
                GLImage(sprite.clone()).getSubimage(textureMesh.offsetX.toInt(), textureMesh.offsetY.toInt(), textureMesh.width.toInt(), textureMesh.height.toInt()) as GLImage
            }
        }
    }

    private fun changeReferenceTo(other: GLImage) {
        sprite = other.sprite
        textureMesh = other.textureMesh
        reference = null
    }

    override fun getGraphics(): FakeGraphics {
        throw UnsupportedOperationException("Texture can't have graphics!")
    }

    fun getBuffer() : IntArray {
        sprite.bind()

        val buffer = IntArray(textureMesh.width.toInt() * textureMesh.height.toInt() * 4)

        GL33.glGetTexImage(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_INT, buffer)

        return buffer
    }
}