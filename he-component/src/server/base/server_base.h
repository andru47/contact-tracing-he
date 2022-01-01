#include <util.h>

class ServerHelper {
public:
    ServerHelper();
    virtual string compute(vector<string> &cipher1, vector<string> &cipher2)=0;
    virtual string computeAltitudeDifference(string &altitude1, string &altitude2)=0;
};
