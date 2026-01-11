import {useState} from "react";
import api from "../api/axios"



function RegisterPage(){
    const [loginId, setLoginId] = useState("");
    const [loginPassword, setLoginPassword] = useState("");
    const [message, setMessage] = useState("");

    // handleRegister used to know the action from the Front end
    // handleRegister vs useEffect
    // handleRegister -> form , button action to back end
    // useEffect -> API DB or DB(SQL) to fetch the data 
    // async -> return promise (asynchronous) 
    // await -> mainly check response error 
    const handleRegister = async(e) => {
        e.preventDefault();

        try{
            const response =  await api.post("/memers/register", {
                loginId,
                loginPassword
            });
            setMessage("sign in complete!" + response.data.memberId);
        }catch(error){
            console.log(error);
            setMessage("fail to sign in");
        }
        };

        return(
            <div className = "flex felx col items-center mt-20">
                <h1 className = "text-3xl font-bold">sign in</h1>

                <form className = "flex flex-col w-80 mt-5" onSubmit ={handleRegister}>
                <input type="text"
                placeholder = "sign in" 
                className = "p-2 border mb-3"
                value = {loginId}
                onChange = {(e) => setLoginId(e.target.value)}/>

                <input type="password" 
                placeholder = "Password"
                className = "p-2 border mb-3"
                value = {loginPassword}
                onChange = {(e) => setLoginPassword(e.target.value)}
                />

                <button className = "bg-blue-600 text-white p-2 rounded">
                    Register
                </button>
                </form>
                {message && <p className = "mt-4 text-red-500">{message}</p>}
            </div>

        )

    }



export default RegisterPage;