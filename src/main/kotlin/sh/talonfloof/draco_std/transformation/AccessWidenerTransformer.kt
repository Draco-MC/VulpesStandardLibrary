package sh.talonfloof.draco_std.transformation

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import sh.talonfloof.dracoloader.transform.IDracoTransformer

class AccessWidenerTransformer : IDracoTransformer {
    override fun transform(loader: ClassLoader, className: String, originalClassData: ByteArray?): ByteArray? {
        if(className == "net.minecraft.world.level.block.entity.BlockEntityType") {
            val reader = ClassReader(originalClassData)
            val node = ClassNode()
            reader.accept(node, 0)
            node.innerClasses.find { it.name == "net/minecraft/world/level/block/entity/BlockEntityType\$BlockEntitySupplier" }!!.access = Opcodes.ACC_PUBLIC
            return ClassWriter(reader, ClassWriter.COMPUTE_FRAMES).also { node.accept(it) }.toByteArray()
        }
        return originalClassData
    }

}