package minecity.forge

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.*

class ForgeListener {

    def register() {

        // MinecraftForge.EVENT_BUS.register this -- Not working due to a class loader problem with ASMEventHandler
        // The code below is an ugly workaround :(

        def listener = this
        getClass().methods.each { method->
            def annotation = method.getAnnotation(SubscribeEvent)
            def params = method.parameterTypes
            if(!annotation || params.length != 1 || !(Event.class.isAssignableFrom(params[0])))
                return

            def sample = params[0].getConstructor().with {
                accessible = true
                (Event) newInstance()
            }

            def busID = EventBus.class.getDeclaredField('busID').with {
                accessible = true
                (int) get(MinecraftForge.EVENT_BUS)
            }

            sample.listenerList.register(busID, annotation.priority(), new IEventListener() {
                @Override
                void invoke(Event event) {
                    method.invoke(listener, event)
                }
            })
        }
    }
}
