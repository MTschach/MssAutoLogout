import React from 'react';
import './App.css';
import Rest from './Rest';

import TreeMenu from 'react-simple-tree-menu';

//import '../node_modules/react-simple-tree-menu/dist/main.css';
import './UserList.css';

export interface ULProps {
    loadUser : (u:string, t:number) => void;
}

interface ULNode{
    key: string;
    label: string;
    maxTime: number;
};

interface ULList {
    key: string;
    label: string;
    nodes: ULNode[];
};

export interface ULState {
    users: ULList[];
}

export default class UserList extends React.Component<ULProps, ULState> {
    constructor(props:ULProps) {
        super(props);
        this.state = {users: []};
        this.loadRest();
    }

    loadRest() {
        let r = new Rest();
        let instance = r.getRestInstance();
        instance.get('v1/users')
     .then(
        (response) => {
            let retVal:ULList[] = [{
                key: 'userlist',
                label: 'UserList',
                nodes: []
            }];
            for (var i=0; i<response.data.userlist.length; i++) {
                let entry : ULNode = {
                    key: response.data.userlist[i].username,
                    label: response.data.userlist[i].username,
                    maxTime: response.data.userlist[i].dailyValue,
                };
                retVal[0].nodes.push(entry);
            }

            this.setState({users : retVal});
           return ;
        },
        (error) => {
            alert(error.toString());
        }
     )
     .catch(error => {
        alert(error.toString());
        }
     );
    

    }

    loadUsers() {
        return [{
            key: 'userlist',
            label: 'UserList',
            nodes: [
                {
                    key: 'simon',
                    label: 'Simon',
                    maxTime: 60,
                },
                {
                    key: 'benjamin',
                    label: 'Benjamin',
                    maxTime: 45,
                }
            ]
        }];
    
    }


    render() {
        console.log("render UserList");
        return (<div className="userlist">
            <TreeMenu data={this.state.users} onClickItem={({key}) => { 
                if (key.split('/').length !== 2) {
                    return;
                }

                let name : string = key.split('/')[1];
                let mt : number = 0;
                for (var i = 0; i<this.state.users[0].nodes.length; i++) {
                    if (name === this.state.users[0].nodes[i].key) {
                        mt = this.state.users[0].nodes[i].maxTime;
                    }
                }

                this.props.loadUser(name, mt);
            }} />
        </div>
        );
    }
}
