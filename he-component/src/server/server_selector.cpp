#include <server_selector.h>

ServerHelper* getHelper() {
    return new LattigoServerHelper();
}
