package com.kennycason.nn.convolution

import com.kennycason.nn.data.image.*
import org.jblas.FloatMatrix
import org.junit.Test


class DeepConvolutedAutoEncoderTest {

    @Test
    fun ninjaTurdle() {
        val image = Image("/data/ninja.png")
        val imageData = MatrixRGBImageEncoder().encode(image)
        // 32 x 24
        // encode layer 2304 x 1728,
        // decode layer 1728 x 2304

        val height = image.height()
        val width = image.width()

        val layer = ConvolutedAutoEncoderFactory.generateConvolutedAutoencoder(
                initialVisibleDim = Dim(rows = 32 * 3, cols = 24),
                layers = 3,
                learningRate = 0.1f
        )

        val start = System.currentTimeMillis()
        (0.. 250).forEach { i ->
            layer.learn(listOf(imageData), 10)
            println("${System.currentTimeMillis() - start}ms")

            val visual = layer.feedForward(imageData)
            val outImage = MatrixRGBImageDecoder(rows = height).decode(visual)
            outImage.save("/tmp/output_ninja_$i.png")
        }
    }

    @Test
    fun jet() {
        val image = Image("/data/fighter_jet_small.jpg")
        val imageData = MatrixRGBImageEncoder().encode(image)
        // 100 x 63
        // encode layer 300 x 63,

        val height = image.height()
        val width = image.width()

        val layer = ConvolutedAutoEncoderFactory.generateConvolutedAutoencoder(
                initialVisibleDim = Dim(rows = 100 * 3, cols = 63),
                layers = 3,
                learningRate = 0.1f
        )

        val start = System.currentTimeMillis()
        var j = 1
        (0.. 10_000).forEach { i ->
            layer.learn(listOf(imageData), 1)
            println("${System.currentTimeMillis() - start}ms")

            if (i > j) {
                val visual = layer.feedForward(imageData)
                val outImage = MatrixRGBImageDecoder(rows = height).decode(visual)
                outImage.save("/tmp/output_jet_$i.png")
                j *= 2
            }
        }
    }

    @Test
    fun pokemon() {
        val xs = CompositeImageReader.read(
                file = "/data/pokemon_151_dark_bg.png",
                matrixImageEncoder = MatrixRGBImageEncoder(),
                rows = 11,
                cols = 15,
                n = 151)
        // images are 60x60 (encode layer x3 rgb = 10800)

        val height = 60
        val width = 60

//        val layer = ConvolutedAutoEncoderFactory.generateConvolutedAutoencoder(
//                initialVisibleDim = Dim(rows = 60 * 3, cols = 60),
//                layers = 5,
//                learningRate = 0.01f
//        )

        val layer1 = ConvolutedLayer(
                visibleDim = Dim(60 * 3, 60),
                hiddenDim = Dim(360, 120),
                paritions = Dim(60, 60),
                learningRate = 0.1f,
                log = false
        )
        val layer2 = ConvolutedLayer(
                visibleDim = Dim(360, 120),
                hiddenDim = Dim(180, 120),
                paritions = Dim(20, 20),
                learningRate = 0.1f,
                log = false
        )
        val layer3 = ConvolutedLayer(
                visibleDim = Dim(180, 120),
                hiddenDim = Dim(40, 40),
                paritions = Dim(20, 20),
                learningRate = 0.1f,
                log = false
        )
//        val layer4 = ConvolutedLayer(
//                visibleDim = Dim(120, 80),
//                hiddenDim = Dim(80, 60),
//                paritions = Dim(20, 20),
//                learningRate = 0.1f,
//                log = false
//        )
//        val layer5 = ConvolutedLayer(
//                visibleDim = Dim(80, 60),
//                hiddenDim = Dim(40, 40),
//                paritions = Dim(20, 20),
//                learningRate = 0.1f,
//                log = false
//        )
//        val layer6 = ConvolutedLayer(
//                visibleDim = Dim(40, 40),
//                hiddenDim = Dim(20, 20),
//                paritions = Dim(10, 10),
//                learningRate = 0.1f,
//                log = false
//        )
//        val layer7 = ConvolutedLayer(
//                visibleDim = Dim(20, 20),
//                hiddenDim = Dim(1, 1),
//                paritions = Dim(1, 1),
//                learningRate = 0.1f,
//                log = false
//        )

        val layer = DeepConvolutedAutoEncoder(
                layers = arrayOf(layer1, layer2, layer3/*, layer4, layer5, layer6*/))

        val start = System.currentTimeMillis()
        var j = 1
        val m = 100
        val n = 136
        (0.. 10_000).forEach { i ->
            layer.learn(xs.subList(m, n), 100)
            println("$i -> ${System.currentTimeMillis() - start}ms")

            if (i > j) {
                val ys = mutableListOf<FloatMatrix>()
                xs.subList(m, n).forEach { x ->
                    ys.add(layer.feedForward(x))
                }
                val image = CompositeImageWriter.write(
                        data = ys,
                        rows = 6, // 11,
                        cols = 6, //15,
                        matrixImageDecoder = MatrixRGBImageDecoder(rows = 60))
                image.save("/tmp/deep_generated_pokemon_$i.png")
                j += 2
            }
        }

    }

}