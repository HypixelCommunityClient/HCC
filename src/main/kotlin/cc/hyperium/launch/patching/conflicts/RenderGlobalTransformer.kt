package cc.hyperium.launch.patching.conflicts

import cc.hyperium.Hyperium
import cc.hyperium.event.Event
import cc.hyperium.event.EventBus
import cc.hyperium.event.render.RenderEntitiesEvent
import cc.hyperium.handlers.HyperiumHandlers
import cc.hyperium.handlers.handlers.OtherConfigOptions
import cc.hyperium.handlers.handlers.cloud.CloudHandler
import cc.hyperium.internal.MemoryHelper
import codes.som.anthony.koffee.assembleBlock
import codes.som.anthony.koffee.insns.jvm.*
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.ResourceLocation
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LdcInsnNode

class RenderGlobalTransformer : ConflictTransformer {
    override fun getClassName() = "bfr"

    override fun transform(original: ClassNode): ClassNode {
        original.methods.forEach {
            when (it.name) {
                "renderEntities" -> {
                    val (postRenderEntities) = assembleBlock {
                        getstatic(EventBus::class, "INSTANCE", EventBus::class)
                        new(RenderEntitiesEvent::class)
                        dup
                        fload_3
                        invokespecial(RenderEntitiesEvent::class, "<init>", void, float)
                        invokevirtual(EventBus::class, "post", void, Event::class)
                    }

                    for (insn in it.instructions.iterator()) {
                        if (insn is LdcInsnNode && insn.cst == "entityOutlines") {
                            it.instructions.insertBefore(insn.next.next, postRenderEntities)
                        }
                    }
                }

                "renderClouds" -> {
                    val (swapForHyperiumRenderer) = assembleBlock {
                        getstatic(Hyperium::class, "INSTANCE", Hyperium::class)
                        invokevirtual(Hyperium::class, "getHandlers", HyperiumHandlers::class)
                        invokevirtual(HyperiumHandlers::class, "getCloudHandler", CloudHandler::class)
                        aload_0
                        getfield(RenderGlobal::class, "cloudTickCounter", int)
                        fload_1
                        invokevirtual(CloudHandler::class, "renderClouds", boolean, int, float)
                        ifeq(L["1"])
                        _return
                        +L["1"]
                    }

                    it.instructions.insertBefore(it.instructions.first, swapForHyperiumRenderer)
                }

                "preRenderDamagedBlocks" -> {
                    for (insn in it.instructions.iterator()) {
                        if (insn is LdcInsnNode) {
                            if (insn.cst == -3.0F && insn.next is LdcInsnNode) {
                                insn.cst = -1.0F
                            } else if (insn.cst == -3.0f && insn.next !is LdcInsnNode) {
                                insn.cst = -10.0F
                            }
                        }
                    }
                }

                "drawSelectionBox" -> {
                    val (cancelBox) = assembleBlock {
                        getstatic(Hyperium::class, "INSTANCE", Hyperium::class)
                        invokevirtual(Hyperium::class, "getHandlers", HyperiumHandlers::class)
                        invokevirtual(HyperiumHandlers::class, "getConfigOptions", OtherConfigOptions::class)
                        getfield(OtherConfigOptions::class, "isCancelBox", boolean)
                        ifeq(L["1"])
                        getstatic(Hyperium::class, "INSTANCE", Hyperium::class)
                        invokevirtual(Hyperium::class, "getHandlers", HyperiumHandlers::class)
                        invokevirtual(HyperiumHandlers::class, "getConfigOptions", OtherConfigOptions::class)
                        iconst_0
                        putfield(OtherConfigOptions::class, "isCancelBox", boolean)
                        _return
                        +L["1"]
                    }

                    it.instructions.insertBefore(it.instructions.first, cancelBox)
                }

                "spawnParticle" -> {
                    val (cancelParticleRender) = assembleBlock {
                        aload_0
                        getfield(RenderGlobal::class, "mc", Minecraft::class)
                        getfield(Minecraft::class, "gameSettings", GameSettings::class)
                        getfield(GameSettings::class, "particleSetting", int)
                        iconst_3
                        if_icmpne(L["0"])
                        _return
                        +L["0"]
                    }

                    it.instructions.insertBefore(it.instructions.first, cancelParticleRender)
                }

                "spawnEntityFX" -> {
                    val (cancelParticleRendering) = assembleBlock {
                        iload(16)
                        iconst_3
                        if_icmpne(L["400"])
                        aconst_null
                        areturn
                        +L["400"]
                    }

                    for (insn in it.instructions.iterator()) {
                        if (insn is FieldInsnNode && insn.name == "particleSetting") {
                            it.instructions.insertBefore(insn.next?.next, cancelParticleRendering)
                        }
                    }
                }

                "onEntityRemoved" -> {
                    val (deleteOldEntityTextures) = assembleBlock {
                        aload_1
                        instanceof(AbstractClientPlayer::class)
                        ifeq(L["1"])
                        getstatic(MemoryHelper::class, "INSTANCE", MemoryHelper::class)
                        aload_1
                        checkcast(AbstractClientPlayer::class)
                        invokevirtual(AbstractClientPlayer::class, "getLocationCape", ResourceLocation::class)
                        invokevirtual(MemoryHelper::class, "queueDelete", void, ResourceLocation::class)
                        getstatic(MemoryHelper::class, "INSTANCE", MemoryHelper::class)
                        aload_1
                        checkcast(AbstractClientPlayer::class)
                        invokevirtual(AbstractClientPlayer::class, "getLocationSkin", ResourceLocation::class)
                        invokevirtual(MemoryHelper::class, "queueDelete", void, ResourceLocation::class)
                        aload_1
                        checkcast(AbstractClientPlayer::class)
                        invokevirtual(AbstractClientPlayer::class, "getPlayerInfo", NetworkPlayerInfo::class)
                        astore_2
                        aload_2
                        ifnonnull(L["6"])
                        _return
                        +L["6"]
                        aload_2
                        iconst_0
                        putfield(NetworkPlayerInfo::class, "playerTexturesLoaded", boolean)
                        aload_2
                        aconst_null
                        putfield(NetworkPlayerInfo::class, "locationCape", ResourceLocation::class)
                        aload_2
                        aconst_null
                        putfield(NetworkPlayerInfo::class, "locationSkin", ResourceLocation::class)
                        +L["1"]
                        _return
                    }

                    it.instructions.insert(deleteOldEntityTextures)
                }
            }
        }

        return original
    }
}
