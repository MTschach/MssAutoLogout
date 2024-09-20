import axios from 'axios';

export default class Rest {
    baseUrl : string = '/autologout/';

    public getRestInstance() {
        return axios.create(
            {
               baseURL: this.baseUrl,
               timeout: 1000,
               headers: {
                    'content-type': 'application/json',
                    'charset': 'utf-8',
                    'Access-Control-Allow-Origin': '*'
                }
            }
        );
    }
}
