import React from 'react';
import { LineChart, XAxis, YAxis, Line } from 'recharts';
import './App.css';
import Rest from './Rest';
import DateObject from 'react-date-object';

export interface UCProps {
    
}

interface UCPoint {
    day:string;
    time:number;
    maxTime:number;
}

export interface UCState {
    userName:string;
    maxTime:number;
    data:UCPoint[];
    timeframe:string;
}

export default class UserChart extends React.Component<UCProps, UCState> {

    apiKeyInput : React.RefObject<HTMLInputElement>;
    timeInput : React.RefObject<HTMLInputElement>;
    reasonInput : React.RefObject<HTMLInputElement>;
    dateInput : React.RefObject<HTMLInputElement>;

    constructor(props:UCProps) {
        super(props);
        this.state = {
            userName: '',
            maxTime:30,
            data:[],
            timeframe:"7"
        };
        this.submitForm = this.submitForm.bind(this);
        this.apiKeyInput = React.createRef();
        this.timeInput = React.createRef();
        this.reasonInput = React.createRef();
        this.dateInput = React.createRef();
    }

    checkInput(apiKey:string, time:string, reason:string):boolean {
        if (apiKey === ""){
            alert("API-Key darf nicht  leer sein");
            return false;
        }
        if (time === "") {
            alert("Zeit darf nicht leer sein");
            return false;
        }
        if (reason === "") {
            alert("Grund darf nicht leer sein");
            return false;
        }
        return true;
    }

    submitForm1(event:React.FormEvent) {
        event.preventDefault();
        

        console.log("hello - " + this.apiKeyInput.current?.value);
        if (this.apiKeyInput.current)
            this.apiKeyInput.current.value = '';
    }

    submitForm(event:React.FormEvent) {
        let apiKey : string = '';
        let time : string = '';
        let reason : string = '';
        let date : string = '';

        event.preventDefault();

        if (this.state.userName === '')
            return;

        if (this.apiKeyInput.current)
            apiKey = this.apiKeyInput.current.value;
        if (this.timeInput.current)
            time = this.timeInput.current.value;
        if (this.reasonInput.current)
            reason = this.reasonInput.current.value;
        if(this.dateInput.current)
            date = this.dateInput.current.value;

        if (this.checkInput(apiKey, time, reason)) {
            this.resetAll();
            if (date === "") {
                let d = new DateObject();
                d.setFormat("YYYY-MM-DD");
                date = d.format().substring(0, 10);
            }
            let r = new Rest();
            let instance = r.getRestInstance();
            instance.post('v1/users/' + this.state.userName + '/counter', 
                {value:time, reason:reason, date:date}, 
                {
                    headers: {"authtoken": apiKey}
                }
            )
            .then(
                (response) => {
                    this.loadUser(this.state.userName, this.state.maxTime, this.state.timeframe);
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
    }


    resetAll() {
        if (this.apiKeyInput.current)
            this.apiKeyInput.current.value = '';
        if (this.timeInput.current)
            this.timeInput.current.value = '';
        if (this.reasonInput.current)
            this.reasonInput.current.value = '';
        if (this.dateInput.current)
            this.dateInput.current.value = '';
    }


    parseDate(d:string):string {
        return d.substr(6,2) + '.' + d.substr(4,2) + '.';
    }

    loadUser(u:string, t:number, f:string) {
        if (u === "") {
            return;
        }

        let r = new Rest();
        let instance = r.getRestInstance();
        instance.get('v1/users/' + u + '/counter?timeFrame=' + f)
     .then(
        (response) => {
            let points : UCPoint[] = [];

            const vals = response.data.counterValues.values;
            for (const [key, value] of Object.entries(vals)) {
                let p : UCPoint = {
                    day: this.parseDate('' + key),
                    time: parseInt('' + value, 10),
                    maxTime: t
                }
                points.push(p);
            }

            this.setState({userName : u, maxTime: t, data: points, timeframe : f});
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


    render() {
        return (<div><h1>{this.state.userName}</h1>
            <LineChart width={600} height={300} data={this.state.data} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                <XAxis dataKey="day" angle={45} textAnchor="start" style={{fontSize: "80%", maxHeight: "150px", height: "50px"}} />
                <YAxis />
                <Line dataKey="time" dot={false} type="monotone" stroke="red" />
                <Line dataKey="maxTime" dot={false} type="monotone" stroke="yellow"/>
                </LineChart>
                <br />
                <span>Anzeigezeitraum</span>&nbsp;<select onChange={e => this.loadUser(this.state.userName, this.state.maxTime, e.target.value)} value={this.state.timeframe} >
                    <option value="7">7 Tage</option>
                    <option value="14">14 Tage</option>
                    <option value="28">28 Tage</option>
                </select>
                <br />
                <br />
                <form onSubmit={e => this.submitForm(e)}>
                    <table width="50%">
                        <thead><tr><th style={{width: '15%', textAlign: 'right'}}></th><th style={{width: '35%', textAlign: 'left'}}></th></tr></thead>
                        <tbody>
                        <tr><td><span>Autorisierung</span></td><td><input type="password" id="apiKey" name="apiKey" ref={this.apiKeyInput} /></td></tr>
                        <tr><td><span>Zeit</span></td><td><input type="text" id="time" name="time" ref={this.timeInput} /></td></tr>
                        <tr><td><span>Grund</span></td><td><input type="text" id="reason" name="reason" ref={this.reasonInput} /></td></tr>
                        <tr><td><span>Datum</span></td><td><input type="text" id="date" name="date" ref={this.dateInput} /></td></tr>
                        <tr><td><input type="submit" value="Speichern" /></td></tr>
                        </tbody>
                    </table>
                </form>
                </div>
            );
    }

    old() {
        return (                <button onClick={this.submitForm}>Speichern</button>
            );
    }
}
