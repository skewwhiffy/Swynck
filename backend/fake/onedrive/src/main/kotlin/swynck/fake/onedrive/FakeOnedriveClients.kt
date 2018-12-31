package swynck.fake.onedrive

import org.http4k.core.HttpHandler
import swynck.real.onedrive.client.OnedriveClients

class FakeOnedriveClients : OnedriveClients {
    override val authClient: HttpHandler
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val graphClient: HttpHandler
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}